package web_prac.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import web_prac.DAO.PlaceDao;
import web_prac.model.Place;
import web_prac.model.Product;
import web_prac.model.Storehouse;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PlaceDaoImpl extends CommonDaoImpl<Place, Integer> implements PlaceDao {

    public PlaceDaoImpl() {
        super(Place.class);
    }

    @Override
    public List<Place> searchPlaces(Integer roomNum, Integer shelfNum, Double minFreeCapacity) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Place> query = builder.createQuery(Place.class);
            Root<Place> place = query.from(Place.class);
            List<Predicate> predicates = new ArrayList<>();

            if (roomNum != null) {
                predicates.add(builder.equal(place.get("roomNum"), roomNum));
            }

            if (shelfNum != null) {
                predicates.add(builder.equal(place.get("shelfNum"), shelfNum));
            }

            if (minFreeCapacity != null) {
                Subquery<Double> usedWeightSubquery = buildUsedWeightSubquery(query, builder, place);
                predicates.add(
                        builder.greaterThanOrEqualTo(
                                builder.diff(place.<Double>get("kgLimit"), builder.coalesce(usedWeightSubquery, 0.0)),
                                minFreeCapacity
                        )
                );
            }

            query.select(place)
                    .where(predicates.toArray(Predicate[]::new))
                    .orderBy(builder.asc(place.get("roomNum")), builder.asc(place.get("shelfNum")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public double getFreeCapacity(Integer placeId) {
        try (Session session = sessionFactory.openSession()) {
            Place place = session.find(Place.class, placeId);
            if (place == null) {
                return 0.0;
            }

            Double usedWeight = getUsedWeight(session, placeId);
            return place.getKgLimit() - (usedWeight == null ? 0.0 : usedWeight);
        }
    }

    @Override
    public boolean hasPlaceForProduct(Integer productId, Double amount) {
        return findFirstSuitablePlace(productId, amount) != null;
    }

    @Override
    public Place findFirstSuitablePlace(Integer productId, Double amount) {
        if (productId == null || amount == null || amount <= 0) {
            return null;
        }

        try (Session session = sessionFactory.openSession()) {
            Product product = session.find(Product.class, productId);
            if (product == null) {
                return null;
            }

            double requiredWeight = product.getKgPerUnit() * amount;
            List<Place> suitablePlaces = searchPlaces(null, null, requiredWeight);
            return suitablePlaces.isEmpty() ? null : suitablePlaces.get(0);
        }
    }

    private Double getUsedWeight(Session session, Integer placeId) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Double> query = builder.createQuery(Double.class);
        Root<Storehouse> storehouse = query.from(Storehouse.class);

        query.select(
                        builder.sum(
                                builder.prod(
                                        storehouse.<Double>get("amount"),
                                        storehouse.get("product").<Double>get("kgPerUnit")
                                )
                        )
                )
                .where(builder.equal(storehouse.get("place").get("id"), placeId));

        return session.createQuery(query).getSingleResult();
    }

    private Subquery<Double> buildUsedWeightSubquery(
            CriteriaQuery<?> query,
            CriteriaBuilder builder,
            Root<Place> place
    ) {
        Subquery<Double> subquery = query.subquery(Double.class);
        Root<Storehouse> storehouse = subquery.from(Storehouse.class);

        subquery.select(
                        builder.sum(
                                builder.prod(
                                        storehouse.<Double>get("amount"),
                                        storehouse.get("product").<Double>get("kgPerUnit")
                                )
                        )
                )
                .where(builder.equal(storehouse.get("place").get("id"), place.get("id")));

        return subquery;
    }
}

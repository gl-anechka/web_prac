package web_prac.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import web_prac.DAO.OperationDao;
import web_prac.DAO.dto.OperationKind;
import web_prac.DAO.dto.OperationView;
import web_prac.model.Partner;
import web_prac.model.Product;
import web_prac.model.Reception;
import web_prac.model.Supply;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
public class OperationDaoImpl extends AbstractHibernateDao implements OperationDao {

    @Override
    public List<OperationView> findByFilter(
            OperationKind kind,
            LocalDateTime from,
            LocalDateTime to,
            Integer partnerId,
            Integer productId
    ) {
        try (Session session = sessionFactory.openSession()) {
            List<OperationView> result = new ArrayList<>();

            if (kind == null || kind == OperationKind.SUPPLY) {
                result.addAll(loadSupplies(session, from, to, partnerId, productId));
            }

            if (kind == null || kind == OperationKind.RECEPTION) {
                result.addAll(loadReceptions(session, from, to, partnerId, productId));
            }

            result.sort(Comparator.comparing(OperationView::getTime).reversed());
            return result;
        }
    }

    @Override
    public List<OperationView> findRecent(int limit) {
        List<OperationView> all = findByFilter(null, null, null, null, null);
        if (limit <= 0 || all.size() <= limit) {
            return all;
        }
        return new ArrayList<>(all.subList(0, limit));
    }

    private List<OperationView> loadSupplies(
            Session session,
            LocalDateTime from,
            LocalDateTime to,
            Integer partnerId,
            Integer productId
    ) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Supply> query = builder.createQuery(Supply.class);
        Root<Supply> supply = query.from(Supply.class);
        Fetch<Supply, Partner> providerFetch = supply.fetch("provider", JoinType.INNER);
        Fetch<Supply, Product> productFetch = supply.fetch("product", JoinType.INNER);
        List<Predicate> predicates = new ArrayList<>();

        if (from != null) {
            predicates.add(builder.greaterThanOrEqualTo(supply.get("time"), from));
        }
        if (to != null) {
            predicates.add(builder.lessThanOrEqualTo(supply.get("time"), to));
        }
        if (partnerId != null) {
            predicates.add(builder.equal(supply.get("provider").get("id"), partnerId));
        }
        if (productId != null) {
            predicates.add(builder.equal(supply.get("product").get("id"), productId));
        }

        query.select(supply)
                .distinct(true)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(builder.desc(supply.get("time")));

        List<Supply> supplies = session.createQuery(query).getResultList();
        List<OperationView> result = new ArrayList<>(supplies.size());
        for (Supply item : supplies) {
            result.add(
                    new OperationView(
                            OperationKind.SUPPLY,
                            item.getId(),
                            item.getTime(),
                            item.getProvider().getId(),
                            item.getProvider().getName(),
                            item.getProduct().getId(),
                            item.getProduct().getTitle(),
                            item.getAmount(),
                            null
                    )
            );
        }
        return result;
    }

    private List<OperationView> loadReceptions(
            Session session,
            LocalDateTime from,
            LocalDateTime to,
            Integer partnerId,
            Integer productId
    ) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Reception> query = builder.createQuery(Reception.class);
        Root<Reception> reception = query.from(Reception.class);
        Fetch<Reception, Partner> consumerFetch = reception.fetch("consumer", JoinType.INNER);
        Fetch<Reception, Product> productFetch = reception.fetch("product", JoinType.INNER);
        List<Predicate> predicates = new ArrayList<>();

        if (from != null) {
            predicates.add(builder.greaterThanOrEqualTo(reception.get("time"), from));
        }
        if (to != null) {
            predicates.add(builder.lessThanOrEqualTo(reception.get("time"), to));
        }
        if (partnerId != null) {
            predicates.add(builder.equal(reception.get("consumer").get("id"), partnerId));
        }
        if (productId != null) {
            predicates.add(builder.equal(reception.get("product").get("id"), productId));
        }

        query.select(reception)
                .distinct(true)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(builder.desc(reception.get("time")));

        List<Reception> receptions = session.createQuery(query).getResultList();
        List<OperationView> result = new ArrayList<>(receptions.size());
        for (Reception item : receptions) {
            result.add(
                    new OperationView(
                            OperationKind.RECEPTION,
                            item.getId(),
                            item.getTime(),
                            item.getConsumer().getId(),
                            item.getConsumer().getName(),
                            item.getProduct().getId(),
                            item.getProduct().getTitle(),
                            item.getAmount(),
                            item.getCompleted()
                    )
            );
        }
        return result;
    }
}

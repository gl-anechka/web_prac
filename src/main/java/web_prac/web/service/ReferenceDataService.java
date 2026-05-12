package web_prac.web.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web_prac.model.Partner;
import web_prac.model.PartnerType;
import web_prac.model.Place;
import web_prac.model.Product;
import web_prac.model.ProductType;
import web_prac.web.view.IdLabelView;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReferenceDataService {
    @PersistenceContext
    private EntityManager entityManager;

    public List<ProductType> getProductTypes() {
        return entityManager.createQuery(
                "select pt from ProductType pt order by pt.title",
                ProductType.class
        ).getResultList();
    }

    public List<IdLabelView> getProductOptions() {
        return entityManager.createQuery(
                "select p from Product p order by p.title",
                Product.class
        ).getResultList().stream()
                .map(product -> new IdLabelView(product.getId(), product.getTitle()))
                .toList();
    }

    public List<IdLabelView> getPlaceOptions() {
        return entityManager.createQuery(
                "select p from Place p order by p.roomNum, p.shelfNum",
                Place.class
        ).getResultList().stream()
                .map(place -> new IdLabelView(place.getId(), formatPlace(place)))
                .toList();
    }

    public List<IdLabelView> getPartnerOptions(PartnerType type) {
        String queryText =
                "select p from Partner p where (:type is null or p.type = :type) order by p.name";

        return entityManager.createQuery(queryText, Partner.class)
                .setParameter("type", type)
                .getResultList().stream()
                .map(partner -> new IdLabelView(partner.getId(), partner.getName()))
                .toList();
    }

    public List<IdLabelView> getPartnerOptionsForProviders() {
        return entityManager.createQuery(
                "select p from Partner p where p.type = :provider or p.type = :both order by p.name",
                Partner.class
        )
                .setParameter("provider", PartnerType.PROVIDER)
                .setParameter("both", PartnerType.BOTH)
                .getResultList().stream()
                .map(partner -> new IdLabelView(partner.getId(), partner.getName()))
                .toList();
    }

    public List<IdLabelView> getPartnerOptionsForConsumers() {
        return entityManager.createQuery(
                "select p from Partner p where p.type = :consumer or p.type = :both order by p.name",
                Partner.class
        )
                .setParameter("consumer", PartnerType.CONSUMER)
                .setParameter("both", PartnerType.BOTH)
                .getResultList().stream()
                .map(partner -> new IdLabelView(partner.getId(), partner.getName()))
                .toList();
    }

    public String formatPlace(Place place) {
        return "Комната " + place.getRoomNum() + " / полка " + place.getShelfNum();
    }
}

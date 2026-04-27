package web_prac.web.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import web_prac.model.ProductType;
import web_prac.web.view.IdLabelView;
import web_prac.model.*;

import java.util.List;

@Service
@Transactional(readOnly = true)
//подготовка данных для страниц
//списки товаров, партнеров, мест хранения, видов товаров
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

    public String formatPlace(Place place) {
        return "Комната " + place.getRoomNum() + " / полка " + place.getShelfNum();
    }
}

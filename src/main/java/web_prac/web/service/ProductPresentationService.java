package web_prac.web.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web_prac.model.Place;
import web_prac.model.Product;
import web_prac.model.StoreStatus;
import web_prac.web.view.ProductRowView;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
//подсчет остатков хранения товаров
public class ProductPresentationService {
    private final ReferenceDataService referenceDataService;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductPresentationService(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public ProductRowView toRow(Product product) {
        Double availableAmount = getAvailableAmount(product.getId());
        String places = findPlaces(product.getId());

        String description = product.getProductType().getDescription();
        String charecteristics = (description == null || description.isBlank())
            ? ("Ед. изм.: " + product.getUnit() + ", вес: " + product.getKgPerUnit() + "кг")
            : (description + " | ед. изм.: " + product.getUnit() + ", вес: " + product.getKgPerUnit() + "кг");

        return new ProductRowView(
            product.getId(),
            product.getTitle(),
            product.getProductType().getTitle(),
            charecteristics,
            product.getUnit().name(),
            product.getKgPerUnit(),
            availableAmount,
            places
        );
    }

    public List<ProductRowView> toRows(List<Product> products) {
        return products.stream().map(this::toRow).toList();
    }

    private Double getAvailableAmount(Integer productId) {
        Double amount = entityManager.createQuery(
            "select sum(s.amount) from Storehouse s where s.product.id = :productId " +
            "and s.amount > 0 and s.status <> :spoiled",
            Double.class
        ).setParameter("productId", productId).setParameter("spoiled", StoreStatus.SPOILED).getSingleResult();

        return amount == null ? 0.0 : amount;
    }

    private String findPlaces(Integer productId) {
        List<Place> places = entityManager.createQuery(
            "select distinct p from Storehouse s join s.place p where s.product.id = :productId " +
             "and s.amount > 0 and s.status <> :spoiled order by p.roomNum, p.shelfNum",
             Place.class
        ).setParameter("productId", productId).setParameter("spoiled", StoreStatus.SPOILED).getResultList();

        if (places.isEmpty()) {
            return "-";
        }

        return places.stream().map(referenceDataService::formatPlace).collect(Collectors.joining(", "));
    }
}

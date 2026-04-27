package web_prac.web.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web_prac.model.Product;
import web_prac.model.ProductType;
import web_prac.model.StoreStatus;
import web_prac.web.form.ProductForm;
import web_prac.web.view.ProductRowView;

import java.util.List;

@Service
public class ProductPageService {
    private final ProductPresentationService productPresentationService;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductPageService(ProductPresentationService productPresentationService) {
        this.productPresentationService = productPresentationService;
    }

    @Transactional(readOnly = true)
    public List<ProductRowView> search(
        String title,
        Integer typeId,
        Boolean inStockOnly,
        StoreStatus status
    ) {
        String normalizedTitle = normalizeForSearch(title);
        String jpql = """
                select distinct p
                from Product p
                left join Storehouse s on s.product = p
                where (:title is null or lower(p.title) like lower(concat('%', :title, '%')))
                  and (:typeId is null or p.productType.id = :typeId)
                  and (:status is null or s.status = :status)
                  and (:inStockOnly = false or s.amount > 0)
                order by p.title
                """;

        List<Product> products = entityManager.createQuery(jpql, Product.class)
                .setParameter("title", normalizedTitle)
                .setParameter("typeId", typeId)
                .setParameter("status", status)
                .setParameter("inStockOnly", Boolean.TRUE.equals(inStockOnly))
                .getResultList();

        return productPresentationService.toRows(products);
    }

    @Transactional(readOnly = true)
    public ProductForm getForm(Integer id) {
        Product product = entityManager.find(Product.class, id);

        if (product == null) {
            throw new BusinessException("Товар не найден");
        }

        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setTitle(product.getTitle());
        form.setProductTypeId(product.getProductType().getId());
        form.setUnit(product.getUnit());
        form.setKgPerUnit(product.getKgPerUnit());

        return form;
    }

    @Transactional
    public void save(ProductForm form) {
        ProductType type = entityManager.find(
                ProductType.class,
                form.getProductTypeId()
        );

        if (type == null) {
            throw new BusinessException("Выбран неизвестный вид товара");
        }

        Product product;

        if (form.getId() == null) {
            product = new Product();
        } else {
            product = entityManager.find(Product.class, form.getId());

            if (product == null) {
                throw new BusinessException("Товар не найден");
            }
        }

        product.setTitle(form.getTitle().trim());
        product.setProductType(type);
        product.setUnit(form.getUnit());
        product.setKgPerUnit(form.getKgPerUnit());

        if (form.getId() == null) {
            entityManager.persist(product);
        }
    }

    @Transactional
    public void delete(Integer id) {
        Product product = entityManager.find(Product.class, id);

        if (product != null) {
            entityManager.remove(product);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeForSearch(String value) {
        String normalized = normalize(value);
        return normalized == null ? "" : normalized;
    }
}

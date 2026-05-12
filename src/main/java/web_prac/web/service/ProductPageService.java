package web_prac.web.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web_prac.DAO.ProductDao;
import web_prac.model.Product;
import web_prac.model.ProductType;
import web_prac.model.StoreStatus;
import web_prac.web.form.ProductForm;
import web_prac.web.view.ProductRowView;

import java.util.List;

@Service
public class ProductPageService {
    private final ProductDao productDao;
    private final ProductPresentationService productPresentationService;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductPageService(ProductDao productDao, ProductPresentationService productPresentationService) {
        this.productDao = productDao;
        this.productPresentationService = productPresentationService;
    }

    @Transactional(readOnly = true)
    public List<ProductRowView> search(
            String title,
            Integer typeId,
            Integer providerId,
            Boolean inStockOnly,
            StoreStatus status,
            Integer placeId
    ) {
        List<Product> products = productDao.searchProduct(
                normalize(title),
                typeId,
                providerId,
                inStockOnly,
                status,
                placeId
        );

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
        ProductType type = entityManager.find(ProductType.class, form.getProductTypeId());

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

        if (product == null) {
            return;
        }

        if (hasReferences(id)) {
            throw new BusinessException("Нельзя удалить товар, пока он участвует в операциях или хранится на складе");
        }

        entityManager.remove(product);
        entityManager.flush();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private boolean hasReferences(Integer productId) {
        Long count = entityManager.createQuery(
                """
                select
                    (select count(s) from Supply s where s.product.id = :productId) +
                    (select count(r) from Reception r where r.product.id = :productId) +
                    (select count(st) from Storehouse st where st.product.id = :productId)
                """,
                Long.class
        )
                .setParameter("productId", productId)
                .getSingleResult();

        return count != null && count > 0;
    }
}

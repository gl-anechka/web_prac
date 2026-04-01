package web_prac.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import web_prac.DAO.ProductDao;
import web_prac.model.Product;
import web_prac.model.StoreStatus;
import web_prac.model.Unit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProductDaoImplTest extends DaoTestSupport {

    @Autowired
    private ProductDao productDao;

    @Test
    void getByIdReturnsExistingProduct() {
        Product product = productDao.getById(1);

        assertNotNull(product);
        assertEquals("L", product.getUnit().name());
    }

    @Test
    void getAllReturnsAllProducts() {
        List<Product> products = productDao.getAll();

        assertEquals(7, products.size());
    }

    @Test
    void savePersistsNewProduct() {
        Product product = new Product();
        product.setTitle("Green tea 100 g");
        product.setProductType(productType(1));
        product.setUnit(Unit.PCS);
        product.setKgPerUnit(2.5);

        Product saved = productDao.save(product);

        assertNotNull(saved.getId());
        assertEquals("Green tea 100 g", productDao.getById(saved.getId()).getTitle());
    }

    @Test
    void updateChangesExistingProduct() {
        Product product = productDao.getById(1);
        product.setTitle("Milk 1 L pasteurized");

        Product updated = productDao.update(product);

        assertEquals("Milk 1 L pasteurized", updated.getTitle());
        assertEquals("Milk 1 L pasteurized", productDao.getById(1).getTitle());
    }

    @Test
    void deleteRemovesProduct() {
        Product product = productDao.save(newProduct("Rice crackers"));

        productDao.delete(product);

        assertNull(productDao.getById(product.getId()));
    }

    @Test
    void deleteByIdRemovesProduct() {
        Product product = productDao.save(newProduct("Laundry gel 2 L"));

        productDao.deleteById(product.getId());

        assertNull(productDao.getById(product.getId()));
    }

    @Test
    void findByTypeReturnsOnlyMatchingProducts() {
        List<Product> products = productDao.findByType(1);

        assertEquals(3, products.size());
        assertTrue(products.stream().allMatch(product -> product.getProductType().getId().equals(1)));
    }

    @Test
    void findByProviderReturnsDistinctProducts() {
        List<Product> products = productDao.findByProvider(1);

        assertEquals(3, products.size());
        assertTrue(products.stream().allMatch(product -> List.of(1, 2, 3).contains(product.getId())));
    }

    @Test
    void findExpiringBeforeReturnsOnlyNonSpoiledProducts() {
        List<Product> products = productDao.findExpiringBefore(LocalDateTime.of(2026, 2, 19, 0, 0));

        assertEquals(1, products.size());
        assertEquals(1, products.get(0).getId());
    }

    @Test
    void searchProductAppliesCombinedFilters() {
        List<Product> products = productDao.searchProduct(
                null,
                4,
                3,
                true,
                StoreStatus.OK,
                null
        );

        assertEquals(1, products.size());
        assertEquals(7, products.get(0).getId());
    }

    @Test
    void searchProductFiltersByPlaceOnly() {
        List<Product> products = productDao.searchProduct(null, null, null, null, null, 4);

        assertEquals(
                Set.of(1, 2),
                products.stream().map(Product::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void searchProductIgnoresBlankTitle() {
        List<Product> products = productDao.searchProduct("   ", null, null, null, null, null);

        assertEquals(7, products.size());
    }

    @Test
    void searchProductFiltersByTitle() {
        List<Product> products = productDao.searchProduct("чайник", null, null, null, null, null);

        assertEquals(1, products.size());
        assertEquals(7, products.get(0).getId());
    }

    @Test
    void searchProductFiltersByProviderOnly() {
        List<Product> products = productDao.searchProduct(null, null, 2, null, null, null);

        assertEquals(2, products.size());
        assertEquals(List.of(4, 5), products.stream().map(Product::getId).toList());
    }

    @Test
    void searchProductDoesNotRestrictWhenInStockOnlyIsFalse() {
        List<Product> products = productDao.searchProduct(null, null, null, false, null, null);

        assertEquals(7, products.size());
    }

    @Test
    void getAvailableAmountSumsOnlyNonSpoiledStock() {
        double amount = productDao.getAvailableAmount(1);

        assertEquals(40.0, amount);
    }

    @Test
    void getAvailableAmountIgnoresZeroAmountEntries() {
        executeUpdate("""
                INSERT INTO storehouse (id_product, amount, id_place, id_supply, received_at, expires_at, status, id_reception)
                VALUES (1, 0, 1, 1, '2026-02-12 10:00:00', '2026-02-17 00:00:00', 'OK', NULL)
                """);

        assertEquals(40.0, productDao.getAvailableAmount(1));
    }

    @Test
    void getAvailableAmountReturnsZeroForUnknownProduct() {
        assertEquals(0.0, productDao.getAvailableAmount(999));
    }

    @Test
    void countAllProductsReturnsTotalCount() {
        assertEquals(7L, productDao.countAllProducts());
    }

    @Test
    void countByTypeReturnsGroupedStats() {
        Map<String, Long> stats = productDao.countByType();

        assertEquals(4, stats.size());
        assertEquals(3L, stats.values().stream().mapToLong(Long::longValue).max().orElseThrow());
        assertEquals(7L, stats.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void countByTypeReturnsEmptyMapWhenThereAreNoProducts() {
        executeUpdate("DELETE FROM storehouse");
        executeUpdate("DELETE FROM reception");
        executeUpdate("DELETE FROM supply");
        executeUpdate("DELETE FROM product");

        assertTrue(productDao.countByType().isEmpty());
    }

    private Product newProduct(String title) {
        Product product = new Product();
        product.setTitle(title);
        product.setProductType(productType(1));
        product.setUnit(Unit.PCS);
        product.setKgPerUnit(1.0);
        return product;
    }
}

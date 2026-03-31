package web_prac.DAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import web_prac.model.Product;
import web_prac.model.StoreStatus;

public interface ProductDao extends CommonDao<Product, Integer> {
    List<Product> findByType(Integer typeId);
    List<Product> findByProvider(Integer partnerId);
    List<Product> findExpiringBefore(LocalDateTime threshold);
    List<Product> searchProduct(String title, Integer typeId, Integer providerId, Boolean inStockOnly, StoreStatus status, Integer placeId);
    double getAvailableAmount(Integer productId);
    long countAllProducts();
    Map<String, Long> countByType();
}
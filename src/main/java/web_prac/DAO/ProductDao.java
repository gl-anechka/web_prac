package web_prac.DAO;

import java.time.LocalDateTime;
import java.util.List;
import web_prac.model.Product;

public interface ProductDao extends CommonDao<Product, Integer> {

    List<Product> findByType(Integer typeId);

    List<Product> findByProvider(Integer partnerId);

    List<Product> findExpiringBefore(LocalDateTime threshold);

    double getAvailableAmount(Integer productId);
}
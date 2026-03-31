package web_prac.DAO;

import web_prac.model.Supply;

import java.time.LocalDateTime;
import java.util.List;

public interface SupplyDao extends CommonDao<Supply, Integer> {

    List<Supply> findByProvider(Integer providerId);

    List<Supply> findByProduct(Integer productId);

    List<Supply> findByPeriod(LocalDateTime from, LocalDateTime to);
}

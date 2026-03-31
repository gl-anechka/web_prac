package web_prac.DAO;

import web_prac.model.Reception;

import java.time.LocalDateTime;
import java.util.List;

public interface ReceptionDao extends CommonDao<Reception, Integer> {

    List<Reception> findByConsumer(Integer consumerId);

    List<Reception> findByProduct(Integer productId);

    List<Reception> findByPeriod(LocalDateTime from, LocalDateTime to);

    List<Reception> findByCompleted(Boolean completed);

    Reception setCompleted(Integer receptionId, Boolean completed);
}

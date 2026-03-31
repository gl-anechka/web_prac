package web_prac.DAO;

import java.time.LocalDateTime;
import java.util.List;
import web_prac.DAO.dto.OperationKind;
import web_prac.DAO.dto.OperationView;

public interface OperationDao {

    List<OperationView> findByFilter(
            OperationKind kind,
            LocalDateTime from,
            LocalDateTime to,
            Integer partnerId,
            Integer productId
    );

    List<OperationView> findRecent(int limit);
}

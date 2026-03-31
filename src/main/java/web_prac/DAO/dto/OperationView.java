package web_prac.DAO.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OperationView {
    private final OperationKind kind;
    private final Integer operationId;
    private final LocalDateTime time;
    private final Integer partnerId;
    private final String partnerName;
    private final Integer productId;
    private final String productTitle;
    private final Double amount;
    private final Boolean completed;
}

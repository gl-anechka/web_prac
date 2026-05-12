package web_prac.web.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import web_prac.DAO.dto.OperationKind;

import java.time.LocalDateTime;

@Getter
@Setter
//поставка и выдача товара
public class OperationForm {
    @NotNull(message = "Выберите тип операции")
    private OperationKind kind;

    @NotNull(message = "Выберите поставщика/получателя")
    private Integer partnerId;

    @NotNull(message = "Выберите товар")
    private Integer productId;

    @NotNull(message = "Укажите дату и время")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime time;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime expiresAt;

    @NotNull(message = "Укажите количество")
    @Positive(message = "Количество должно быть больше нуля")
    private Double amount;
}

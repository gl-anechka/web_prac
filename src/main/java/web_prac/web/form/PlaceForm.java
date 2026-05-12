package web_prac.web.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceForm {
    private Integer id;

    @NotNull(message = "Укажите номер комнаты")
    @Positive(message = "Номер комнаты должен быть больше нуля")
    private Integer roomNum;

    @NotNull(message = "Укажите номер полки")
    @Positive(message = "Номер полки должен быть больше нуля")
    private Integer shelfNum;

    @NotNull(message = "Укажите вместимость полки")
    @Positive(message = "Вместимость должна быть больше нуля")
    private Double kgLimit;
}

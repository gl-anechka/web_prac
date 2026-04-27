package web_prac.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import web_prac.model.Unit;

@Getter
@Setter
//форма товара
public class ProductForm {
    private Integer id;

    @NotBlank(message = "Укажите наименование товара")
    private String title;

    @NotNull(message = "Выберите вид товара")
    private Integer productTypeId;

    @NotNull(message = "Выберите единицу измерения")
    private Unit unit;

    @NotNull(message = "Укажите вес единицы товара")
    @PositiveOrZero(message = "Вес не может быть отрицательным")
    private Double kgPerUnit;
}

package web_prac.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import web_prac.model.PartnerType;

@Getter
@Setter
public class PartnerForm {
    private Integer id;

    @NotNull(message = "Выберите тип партнера")
    private PartnerType type;

    @NotBlank(message = "Укажите наименование партнера")
    @Size(max = 100, message = "Наименование не должно превышать 100 символов")
    private String name;

    @Size(max = 1000, message = "Адрес слишком длинный")
    private String address;

    @Size(max = 12, message = "Телефон не должен превышать 12 символов")
    @Pattern(regexp = "^$|^\\d{10,12}$", message = "Телефон должен содержать от 10 до 12 цифр")
    private String phone;

    @Size(max = 100, message = "E-mail не должен превышать 100 символов")
    @Pattern(regexp = "^$|^[^@\\s]+@[^@\\s]+$", message = "E-mail должен содержать @")
    private String email;
}

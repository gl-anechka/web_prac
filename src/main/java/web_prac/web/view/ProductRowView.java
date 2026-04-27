package web_prac.web.view;

//удобство хранения данных о товаре на странице
public record ProductRowView(
    Integer id,
    String title,
    String typeTitle,
    String characteristics,
    String unit,
    Double kgPerUnit,
    Double availableAmount,
    String places
) {
}

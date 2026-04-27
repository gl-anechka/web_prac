package web_prac.web.view;

//таблица мест хранения
public record PlaceRowView(
    Integer id,
    Integer roomNum,
    Integer shelNum,
    Double kgLimit,
    Double freeCapacity
) {
}

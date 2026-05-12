package web_prac.web.view;

public record PlaceRowView(
        Integer id,
        Integer roomNum,
        Integer shelfNum,
        Double kgLimit,
        Double freeCapacity
) {
}

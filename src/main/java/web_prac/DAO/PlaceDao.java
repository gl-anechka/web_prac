package web_prac.DAO;

import java.util.List;
import web_prac.model.Place;

public interface PlaceDao extends CommonDao<Place, Integer> {

    List<Place> searchPlaces(Integer roomNum, Integer shelfNum, Double minFreeCapacity);

    double getFreeCapacity(Integer placeId);

    boolean hasPlaceForProduct(Integer productId, Double amount);

    Place findFirstSuitablePlace(Integer productId, Double amount);
}

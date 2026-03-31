package web_prac.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import web_prac.DAO.PlaceDao;
import web_prac.model.Place;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaceDaoImplTest extends DaoTestSupport {

    @Autowired
    private PlaceDao placeDao;

    @Test
    void getByIdReturnsPlace() {
        assertEquals(1, placeDao.getById(1).getId());
    }

    @Test
    void getAllReturnsAllPlaces() {
        assertEquals(5, placeDao.getAll().size());
    }

    @Test
    void savePersistsPlace() {
        Place place = new Place();
        place.setRoomNum(3);
        place.setShelfNum(3);
        place.setKgLimit(250.0);

        Place saved = placeDao.save(place);

        assertNotNull(saved.getId());
        assertEquals(250.0, placeDao.getById(saved.getId()).getKgLimit());
    }

    @Test
    void updateChangesPlace() {
        Place place = placeDao.getById(1);
        place.setKgLimit(333.0);

        placeDao.update(place);

        assertEquals(333.0, placeDao.getById(1).getKgLimit());
    }

    @Test
    void deleteRemovesPlace() {
        Place place = new Place();
        place.setRoomNum(4);
        place.setShelfNum(4);
        place.setKgLimit(50.0);
        Place saved = placeDao.save(place);

        placeDao.delete(saved);

        assertNull(placeDao.getById(saved.getId()));
    }

    @Test
    void deleteByIdRemovesPlace() {
        Place place = new Place();
        place.setRoomNum(5);
        place.setShelfNum(5);
        place.setKgLimit(60.0);
        Place saved = placeDao.save(place);

        placeDao.deleteById(saved.getId());

        assertNull(placeDao.getById(saved.getId()));
    }

    @Test
    void searchPlacesFiltersByFreeCapacity() {
        List<Place> places = placeDao.searchPlaces(null, null, 100.0);

        assertEquals(3, places.size());
        assertTrue(places.stream().allMatch(place -> List.of(1, 2, 5).contains(place.getId())));
    }

    @Test
    void getFreeCapacityReturnsRemainingWeight() {
        assertEquals(77.0, placeDao.getFreeCapacity(4));
    }

    @Test
    void hasPlaceForProductChecksCapacity() {
        assertTrue(placeDao.hasPlaceForProduct(7, 60.0));
        assertFalse(placeDao.hasPlaceForProduct(7, 200.0));
    }

    @Test
    void findFirstSuitablePlaceReturnsFirstMatchingPlace() {
        Place place = placeDao.findFirstSuitablePlace(7, 60.0);

        assertNotNull(place);
        assertEquals(1, place.getId());
    }
}

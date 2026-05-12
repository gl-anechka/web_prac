package web_prac.web.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web_prac.DAO.PlaceDao;
import web_prac.model.Place;
import web_prac.web.form.PlaceForm;
import web_prac.web.view.PlaceRowView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlacePageService {

    private final PlaceDao placeDao;

    @PersistenceContext
    private EntityManager entityManager;

    public PlacePageService(PlaceDao placeDao) {
        this.placeDao = placeDao;
    }

    @Transactional(readOnly = true)
    public List<PlaceRowView> search(Integer roomNum, Integer shelfNum, Double minFreeCapacity) {
        List<Place> places = placeDao.searchPlaces(roomNum, shelfNum, minFreeCapacity);
        Map<Integer, Double> usedCapacityByPlace = loadUsedCapacityByPlace(places);

        return places.stream()
                .map(place -> new PlaceRowView(
                        place.getId(),
                        place.getRoomNum(),
                        place.getShelfNum(),
                        place.getKgLimit(),
                        place.getKgLimit() - usedCapacityByPlace.getOrDefault(place.getId(), 0.0)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaceForm getForm(Integer id) {
        Place place = entityManager.find(Place.class, id);
        if (place == null) {
            throw new BusinessException("Место хранения не найдено");
        }

        PlaceForm form = new PlaceForm();
        form.setId(place.getId());
        form.setRoomNum(place.getRoomNum());
        form.setShelfNum(place.getShelfNum());
        form.setKgLimit(place.getKgLimit());
        return form;
    }

    @Transactional
    public void save(PlaceForm form) {
        validateUniqueness(form);

        Place place = form.getId() == null ? new Place() : entityManager.find(Place.class, form.getId());
        if (place == null) {
            throw new BusinessException("Место хранения не найдено");
        }

        place.setRoomNum(form.getRoomNum());
        place.setShelfNum(form.getShelfNum());
        place.setKgLimit(form.getKgLimit());

        if (form.getId() == null) {
            entityManager.persist(place);
        }
    }

    @Transactional
    public void delete(Integer id) {
        Place place = entityManager.find(Place.class, id);
        if (place == null) {
            return;
        }

        try {
            entityManager.remove(place);
            entityManager.flush();
        } catch (RuntimeException e) {
            throw new BusinessException("Нельзя удалить место хранения, пока оно используется");
        }
    }

    private void validateUniqueness(PlaceForm form) {
        Long count = entityManager.createQuery(
                """
                select count(p)
                from Place p
                where p.roomNum = :roomNum
                  and p.shelfNum = :shelfNum
                  and (:id is null or p.id <> :id)
                """,
                Long.class
        )
                .setParameter("roomNum", form.getRoomNum())
                .setParameter("shelfNum", form.getShelfNum())
                .setParameter("id", form.getId())
                .getSingleResult();

        if (count != null && count > 0) {
            throw new BusinessException("Такое место хранения уже существует");
        }
    }

    private Map<Integer, Double> loadUsedCapacityByPlace(List<Place> places) {
        if (places.isEmpty()) {
            return Map.of();
        }

        List<Integer> placeIds = places.stream().map(Place::getId).toList();
        List<Object[]> rows = entityManager.createQuery(
                """
                select s.place.id,
                       sum(s.amount * s.product.kgPerUnit)
                from Storehouse s
                where s.place.id in :placeIds
                group by s.place.id
                """,
                Object[].class
        )
                .setParameter("placeIds", placeIds)
                .getResultList();

        Map<Integer, Double> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((Integer) row[0], ((Number) row[1]).doubleValue());
        }

        return result;
    }
}

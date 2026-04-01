package web_prac.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import web_prac.DAO.SupplyDao;
import web_prac.model.Supply;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplyDaoImplTest extends DaoTestSupport {

    @Autowired
    private SupplyDao supplyDao;

    @Test
    void getByIdReturnsSupply() {
        assertEquals(1, supplyDao.getById(1).getId());
    }

    @Test
    void getAllReturnsAllSupplies() {
        assertEquals(6, supplyDao.getAll().size());
    }

    @Test
    void savePersistsSupply() {
        Supply supply = new Supply();
        supply.setProvider(partner(1));
        supply.setProduct(product(1));
        supply.setTime(LocalDateTime.of(2026, 3, 1, 10, 0));
        supply.setAmount(15.0);

        Supply saved = supplyDao.save(supply);

        assertNotNull(saved.getId());
        assertEquals(15.0, supplyDao.getById(saved.getId()).getAmount());
    }

    @Test
    void updateChangesSupply() {
        Supply supply = supplyDao.getById(1);
        supply.setAmount(81.0);

        supplyDao.update(supply);

        assertEquals(81.0, supplyDao.getById(1).getAmount());
    }

    @Test
    void deleteRemovesSupply() {
        Supply supply = new Supply();
        supply.setProvider(partner(1));
        supply.setProduct(product(3));
        supply.setTime(LocalDateTime.of(2026, 3, 2, 10, 0));
        supply.setAmount(5.0);
        Supply saved = supplyDao.save(supply);

        supplyDao.delete(saved);

        assertNull(supplyDao.getById(saved.getId()));
    }

    @Test
    void deleteByIdRemovesSupply() {
        Supply supply = new Supply();
        supply.setProvider(partner(2));
        supply.setProduct(product(4));
        supply.setTime(LocalDateTime.of(2026, 3, 3, 10, 0));
        supply.setAmount(6.0);
        Supply saved = supplyDao.save(supply);

        supplyDao.deleteById(saved.getId());

        assertNull(supplyDao.getById(saved.getId()));
    }

    @Test
    void findByProviderReturnsMatchingSupplies() {
        List<Supply> supplies = supplyDao.findByProvider(1);

        assertEquals(3, supplies.size());
        assertTrue(supplies.stream().allMatch(supply -> supply.getProvider().getId().equals(1)));
    }

    @Test
    void findByProductReturnsMatchingSupplies() {
        List<Supply> supplies = supplyDao.findByProduct(1);

        assertEquals(1, supplies.size());
        assertEquals(1, supplies.get(0).getId());
    }

    @Test
    void findByPeriodReturnsOnlySuppliesInsideRange() {
        List<Supply> supplies = supplyDao.findByPeriod(
                LocalDateTime.of(2026, 2, 2, 0, 0),
                LocalDateTime.of(2026, 2, 9, 23, 59)
        );

        assertEquals(3, supplies.size());
    }

    @Test
    void findByPeriodWithOnlyFromWorks() {
        List<Supply> supplies = supplyDao.findByPeriod(LocalDateTime.of(2026, 2, 5, 0, 0), null);

        assertEquals(3, supplies.size());
        assertTrue(supplies.stream().allMatch(supply -> !supply.getTime().isBefore(LocalDateTime.of(2026, 2, 5, 0, 0))));
    }

    @Test
    void findByPeriodWithOnlyToWorks() {
        List<Supply> supplies = supplyDao.findByPeriod(null, LocalDateTime.of(2026, 2, 1, 10, 30));

        assertEquals(2, supplies.size());
        assertEquals(List.of(2, 4), supplies.stream().map(Supply::getId).toList());
    }

    @Test
    void findByPeriodWithoutBoundsReturnsAll() {
        assertEquals(6, supplyDao.findByPeriod(null, null).size());
    }
}

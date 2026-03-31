package web_prac.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import web_prac.DAO.ReceptionDao;
import web_prac.model.Reception;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReceptionDaoImplTest extends DaoTestSupport {

    @Autowired
    private ReceptionDao receptionDao;

    @Test
    void getByIdReturnsReception() {
        assertEquals(1, receptionDao.getById(1).getId());
    }

    @Test
    void getAllReturnsAllReceptions() {
        assertEquals(5, receptionDao.getAll().size());
    }

    @Test
    void savePersistsReception() {
        Reception reception = new Reception();
        reception.setConsumer(partner(4));
        reception.setProduct(product(1));
        reception.setTime(LocalDateTime.of(2026, 3, 1, 15, 0));
        reception.setAmount(5.0);
        reception.setCompleted(false);

        Reception saved = receptionDao.save(reception);

        assertNotNull(saved.getId());
        assertEquals(5.0, receptionDao.getById(saved.getId()).getAmount());
    }

    @Test
    void updateChangesReception() {
        Reception reception = receptionDao.getById(4);
        reception.setAmount(35.0);

        receptionDao.update(reception);

        assertEquals(35.0, receptionDao.getById(4).getAmount());
    }

    @Test
    void deleteRemovesReception() {
        Reception reception = new Reception();
        reception.setConsumer(partner(4));
        reception.setProduct(product(4));
        reception.setTime(LocalDateTime.of(2026, 3, 2, 15, 0));
        reception.setAmount(2.0);
        reception.setCompleted(false);
        Reception saved = receptionDao.save(reception);

        receptionDao.delete(saved);

        assertNull(receptionDao.getById(saved.getId()));
    }

    @Test
    void deleteByIdRemovesReception() {
        Reception reception = new Reception();
        reception.setConsumer(partner(5));
        reception.setProduct(product(2));
        reception.setTime(LocalDateTime.of(2026, 3, 3, 15, 0));
        reception.setAmount(1.0);
        reception.setCompleted(false);
        Reception saved = receptionDao.save(reception);

        receptionDao.deleteById(saved.getId());

        assertNull(receptionDao.getById(saved.getId()));
    }

    @Test
    void findByConsumerReturnsMatchingReceptions() {
        List<Reception> receptions = receptionDao.findByConsumer(4);

        assertEquals(2, receptions.size());
        assertTrue(receptions.stream().allMatch(reception -> reception.getConsumer().getId().equals(4)));
    }

    @Test
    void findByProductReturnsMatchingReceptions() {
        List<Reception> receptions = receptionDao.findByProduct(1);

        assertEquals(2, receptions.size());
    }

    @Test
    void findByPeriodReturnsOnlyReceptionsInsideRange() {
        List<Reception> receptions = receptionDao.findByPeriod(
                LocalDateTime.of(2026, 2, 11, 0, 0),
                LocalDateTime.of(2026, 2, 12, 23, 59)
        );

        assertEquals(3, receptions.size());
    }

    @Test
    void findByCompletedReturnsMatchingStatus() {
        List<Reception> receptions = receptionDao.findByCompleted(false);

        assertEquals(2, receptions.size());
        assertTrue(receptions.stream().noneMatch(Reception::getCompleted));
    }

    @Test
    void setCompletedUpdatesFlag() {
        Reception updated = receptionDao.setCompleted(4, true);

        assertNotNull(updated);
        assertTrue(receptionDao.getById(4).getCompleted());
    }
}

package web_prac.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import web_prac.DAO.OperationDao;
import web_prac.DAO.dto.OperationKind;
import web_prac.DAO.dto.OperationView;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperationDaoImplTest extends DaoTestSupport {

    @Autowired
    private OperationDao operationDao;

    @Test
    void findByFilterReturnsAllOperationsInDescendingOrder() {
        List<OperationView> operations = operationDao.findByFilter(null, null, null, null, null);

        assertEquals(11, operations.size());
        assertEquals(OperationKind.RECEPTION, operations.get(0).getKind());
        assertEquals(4, operations.get(0).getOperationId());
        assertTrue(operations.get(0).getTime().isAfter(operations.get(1).getTime())
                || operations.get(0).getTime().isEqual(operations.get(1).getTime()));
    }

    @Test
    void findByFilterAppliesKindAndPartnerFilters() {
        List<OperationView> operations = operationDao.findByFilter(
                OperationKind.SUPPLY,
                null,
                null,
                1,
                null
        );

        assertEquals(3, operations.size());
        assertTrue(operations.stream().allMatch(operation -> operation.getKind() == OperationKind.SUPPLY));
        assertTrue(operations.stream().allMatch(operation -> operation.getPartnerId().equals(1)));
    }

    @Test
    void findByFilterAppliesDateAndProductFilters() {
        List<OperationView> operations = operationDao.findByFilter(
                OperationKind.RECEPTION,
                LocalDateTime.of(2026, 2, 11, 0, 0),
                LocalDateTime.of(2026, 2, 12, 23, 59),
                null,
                4
        );

        assertEquals(1, operations.size());
        assertEquals(OperationKind.RECEPTION, operations.get(0).getKind());
        assertEquals(4, operations.get(0).getProductId());
        assertFalse(operations.get(0).getCompleted());
    }

    @Test
    void findRecentReturnsLimitedSlice() {
        List<OperationView> operations = operationDao.findRecent(3);

        assertEquals(3, operations.size());
        assertEquals(4, operations.get(0).getOperationId());
        assertEquals(5, operations.get(1).getOperationId());
        assertEquals(1, operations.get(2).getOperationId());
    }
}

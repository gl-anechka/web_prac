package web_prac.DAO.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import web_prac.model.Product;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonDaoImplTest {

    @Test
    void deleteUsesManagedEntityWithoutMerge() {
        ProductDaoImpl dao = new ProductDaoImpl();
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Transaction transaction = mock(Transaction.class);
        Product entity = new Product();

        dao.sessionFactory = sessionFactory;

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.getTransaction()).thenReturn(transaction);
        when(session.contains(entity)).thenReturn(true);

        assertDoesNotThrow(() -> dao.delete(entity));

        verify(session, never()).merge(entity);
        verify(session).remove(entity);
        verify(transaction).commit();
    }

    @Test
    void saveRollsBackTransactionOnPersistFailure() {
        ProductDaoImpl dao = new ProductDaoImpl();
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Transaction transaction = mock(Transaction.class);
        Product entity = new Product();

        dao.sessionFactory = sessionFactory;

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.getTransaction()).thenReturn(transaction);
        org.mockito.Mockito.doThrow(new RuntimeException("persist failed")).when(session).persist(entity);

        assertThrows(RuntimeException.class, () -> dao.save(entity));

        verify(transaction).rollback();
    }

    @Test
    void updateRollsBackTransactionOnMergeFailure() {
        ProductDaoImpl dao = new ProductDaoImpl();
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Transaction transaction = mock(Transaction.class);
        Product entity = new Product();

        dao.sessionFactory = sessionFactory;

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.getTransaction()).thenReturn(transaction);
        when(session.merge(entity)).thenThrow(new RuntimeException("merge failed"));

        assertThrows(RuntimeException.class, () -> dao.update(entity));

        verify(transaction).rollback();
    }

    @Test
    void deleteRollsBackTransactionOnRemoveFailure() {
        ProductDaoImpl dao = new ProductDaoImpl();
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Transaction transaction = mock(Transaction.class);
        Product entity = new Product();

        dao.sessionFactory = sessionFactory;

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.getTransaction()).thenReturn(transaction);
        when(session.contains(entity)).thenReturn(true);
        org.mockito.Mockito.doThrow(new RuntimeException("remove failed")).when(session).remove(entity);

        assertThrows(RuntimeException.class, () -> dao.delete(entity));

        verify(transaction).rollback();
    }

    @Test
    void deleteByIdSkipsRemoveWhenEntityMissing() {
        ProductDaoImpl dao = new ProductDaoImpl();
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Transaction transaction = mock(Transaction.class);

        dao.sessionFactory = sessionFactory;

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.getTransaction()).thenReturn(transaction);
        when(session.find(Product.class, 123)).thenReturn(null);

        assertDoesNotThrow(() -> dao.deleteById(123));

        verify(session, never()).remove(org.mockito.Mockito.any());
        verify(transaction).commit();
    }

    @Test
    void deleteByIdRollsBackTransactionOnFindFailure() {
        ProductDaoImpl dao = new ProductDaoImpl();
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        Transaction transaction = mock(Transaction.class);

        dao.sessionFactory = sessionFactory;

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.getTransaction()).thenReturn(transaction);
        when(session.find(Product.class, 123)).thenThrow(new RuntimeException("find failed"));

        assertThrows(RuntimeException.class, () -> dao.deleteById(123));

        verify(transaction).rollback();
    }
}

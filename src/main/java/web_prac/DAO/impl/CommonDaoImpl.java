package web_prac.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import web_prac.DAO.CommonDao;

import java.util.List;

public abstract class CommonDaoImpl<T, ID> extends AbstractHibernateDao implements CommonDao<T, ID> {

    private final Class<T> persistentClass;

    protected CommonDaoImpl(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
    }

    @Override
    public T getById(ID id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(persistentClass, id);
        }
    }

    @Override
    public List<T> getAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> query = builder.createQuery(persistentClass);
            Root<T> root = query.from(persistentClass);
            query.select(root);
            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public T save(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try {
                session.persist(entity);
                session.getTransaction().commit();
                return entity;
            } catch (RuntimeException e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T update(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try {
                T merged = (T) session.merge(entity);
                session.getTransaction().commit();
                return merged;
            } catch (RuntimeException e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void delete(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try {
                T managed = entity;
                if (!session.contains(entity)) {
                    managed = (T) session.merge(entity);
                }
                session.remove(managed);
                session.getTransaction().commit();
            } catch (RuntimeException e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public void deleteById(ID id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try {
                T entity = session.find(persistentClass, id);
                if (entity != null) {
                    session.remove(entity);
                }
                session.getTransaction().commit();
            } catch (RuntimeException e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }
}
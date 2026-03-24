package web_prac.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import web_prac.DAO.CommonDao;

public abstract class CommonDaoImpl<T, ID> extends AbstractHibernateDao implements CommonDao<T, ID> {

    private final Class<T> persistentClass;

    protected CommonDaoImpl(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
    }

    @Override
    public T getById(ID id) {
        return currentSession().get(persistentClass, (Object) id);
    }

    @Override
    public List<T> getAll() {
        CriteriaBuilder builder = currentSession().getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(persistentClass);
        Root<T> root = query.from(persistentClass);
        query.select(root);
        return currentSession().createQuery(query).getResultList();
    }

    @Override
    public T save(T entity) {
        currentSession().persist(entity);
        currentSession().flush();
        return entity;
    }

    @Override
    public T update(T entity) {
        T merged = (T) currentSession().merge(entity);
        currentSession().flush();
        return merged;
    }

    @Override
    public void delete(T entity) {
        T managed = entity;
        if (!currentSession().contains(entity)) {
            managed = (T) currentSession().merge(entity);
        }
        currentSession().remove(managed);
        currentSession().flush();
    }

    @Override
    public void deleteById(ID id) {
        T entity = getById(id);
        if (entity != null) {
            currentSession().remove(entity);
            currentSession().flush();
        }
    }
}
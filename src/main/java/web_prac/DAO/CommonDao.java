package web_prac.DAO;

import java.util.List;

public interface CommonDao<T, ID> {
    T getById(ID id);
    List<T> getAll();
    T save(T entity);
    T update(T entity);
    void delete(T entity);
    void deleteById(ID id);
}
package web_prac.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import web_prac.DAO.ReceptionDao;
import web_prac.model.Reception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReceptionDaoImpl extends CommonDaoImpl<Reception, Integer> implements ReceptionDao {

    public ReceptionDaoImpl() {
        super(Reception.class);
    }

    @Override
    public List<Reception> findByConsumer(Integer consumerId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Reception> query = builder.createQuery(Reception.class);
            Root<Reception> reception = query.from(Reception.class);

            query.select(reception)
                    .where(builder.equal(reception.get("consumer").get("id"), consumerId))
                    .orderBy(builder.desc(reception.get("time")), builder.desc(reception.get("id")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public List<Reception> findByProduct(Integer productId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Reception> query = builder.createQuery(Reception.class);
            Root<Reception> reception = query.from(Reception.class);

            query.select(reception)
                    .where(builder.equal(reception.get("product").get("id"), productId))
                    .orderBy(builder.desc(reception.get("time")), builder.desc(reception.get("id")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public List<Reception> findByPeriod(LocalDateTime from, LocalDateTime to) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Reception> query = builder.createQuery(Reception.class);
            Root<Reception> reception = query.from(Reception.class);
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(reception.get("time"), from));
            }
            if (to != null) {
                predicates.add(builder.lessThanOrEqualTo(reception.get("time"), to));
            }

            query.select(reception)
                    .where(predicates.toArray(Predicate[]::new))
                    .orderBy(builder.desc(reception.get("time")), builder.desc(reception.get("id")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public List<Reception> findByCompleted(Boolean completed) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Reception> query = builder.createQuery(Reception.class);
            Root<Reception> reception = query.from(Reception.class);

            query.select(reception)
                    .where(builder.equal(reception.get("completed"), completed))
                    .orderBy(builder.desc(reception.get("time")), builder.desc(reception.get("id")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public Reception setCompleted(Integer receptionId, Boolean completed) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            try {
                Reception reception = session.find(Reception.class, receptionId);
                if (reception == null) {
                    session.getTransaction().commit();
                    return null;
                }

                reception.setCompleted(Boolean.TRUE.equals(completed));
                session.getTransaction().commit();
                return reception;
            } catch (RuntimeException e) {
                session.getTransaction().rollback();
                throw e;
            }
        } finally {
            session.close();
        }
    }
}

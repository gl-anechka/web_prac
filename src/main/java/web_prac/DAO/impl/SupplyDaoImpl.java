package web_prac.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import web_prac.DAO.SupplyDao;
import web_prac.model.Supply;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SupplyDaoImpl extends CommonDaoImpl<Supply, Integer> implements SupplyDao {

    public SupplyDaoImpl() {
        super(Supply.class);
    }

    @Override
    public List<Supply> findByProvider(Integer providerId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Supply> query = builder.createQuery(Supply.class);
            Root<Supply> supply = query.from(Supply.class);

            query.select(supply)
                    .where(builder.equal(supply.get("provider").get("id"), providerId))
                    .orderBy(builder.desc(supply.get("time")), builder.desc(supply.get("id")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public List<Supply> findByProduct(Integer productId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Supply> query = builder.createQuery(Supply.class);
            Root<Supply> supply = query.from(Supply.class);

            query.select(supply)
                    .where(builder.equal(supply.get("product").get("id"), productId))
                    .orderBy(builder.desc(supply.get("time")), builder.desc(supply.get("id")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public List<Supply> findByPeriod(LocalDateTime from, LocalDateTime to) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Supply> query = builder.createQuery(Supply.class);
            Root<Supply> supply = query.from(Supply.class);
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(supply.get("time"), from));
            }
            if (to != null) {
                predicates.add(builder.lessThanOrEqualTo(supply.get("time"), to));
            }

            query.select(supply)
                    .where(predicates.toArray(Predicate[]::new))
                    .orderBy(builder.desc(supply.get("time")), builder.desc(supply.get("id")));

            return session.createQuery(query).getResultList();
        }
    }
}

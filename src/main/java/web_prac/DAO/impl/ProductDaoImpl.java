package web_prac.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import web_prac.DAO.ProductDao;
import web_prac.model.Product;
import web_prac.model.StoreStatus;
import web_prac.model.Storehouse;
import web_prac.model.Supply;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductDaoImpl extends CommonDaoImpl<Product, Integer> implements ProductDao {

    public ProductDaoImpl() {
        super(Product.class);
    }

    @Override
    public List<Product> findByType(Integer typeId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Product> query = builder.createQuery(Product.class);
            Root<Product> product = query.from(Product.class);

            query.select(product)
                    .where(builder.equal(product.get("productType").get("id"), typeId))
                    .orderBy(builder.asc(product.get("title")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public List<Product> findByProvider(Integer partnerId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Product> query = builder.createQuery(Product.class);
            Root<Product> product = query.from(Product.class);

            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Supply> supply = subquery.from(Supply.class);

            subquery.select(builder.literal(1))
                    .where(
                            builder.and(
                                    builder.equal(supply.get("product").get("id"), product.get("id")),
                                    builder.equal(supply.get("provider").get("id"), partnerId)
                            )
                    );

            query.select(product)
                    .distinct(true)
                    .where(builder.exists(subquery))
                    .orderBy(builder.asc(product.get("title")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public List<Product> findExpiringBefore(LocalDateTime threshold) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Product> query = builder.createQuery(Product.class);
            Root<Storehouse> entry = query.from(Storehouse.class);

            query.select(entry.get("product"))
                    .distinct(true)
                    .where(
                            builder.and(
                                    builder.isNotNull(entry.get("expiresAt")),
                                    builder.lessThanOrEqualTo(entry.get("expiresAt"), threshold),
                                    builder.notEqual(entry.get("status"), StoreStatus.SPOILED)
                            )
                    )
                    .orderBy(builder.asc(entry.get("product").get("title")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public double getAvailableAmount(Integer productId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Double> query = builder.createQuery(Double.class);
            Root<Storehouse> entry = query.from(Storehouse.class);

            query.select(builder.sum(entry.<Double>get("amount")))
                    .where(
                            builder.and(
                                    builder.equal(entry.get("product").get("id"), productId),
                                    builder.notEqual(entry.get("status"), StoreStatus.SPOILED)
                            )
                    );

            Double amount = session.createQuery(query).getSingleResult();
            return amount == null ? 0.0 : amount;
        }
    }
}
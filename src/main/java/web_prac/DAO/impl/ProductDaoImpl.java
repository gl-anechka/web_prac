package web_prac.DAO.impl;

import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import web_prac.DAO.ProductDao;
import web_prac.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProductDaoImpl extends CommonDaoImpl<Product, Integer> implements ProductDao {
    private Predicate existsSupplyFromProvider(CriteriaQuery<?> query, CriteriaBuilder builder, Root<Product> product, Integer providerId) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Supply> supply = subquery.from(Supply.class);

        subquery.select(builder.literal(1))
                .where(builder.and(
                        builder.equal(supply.get("product").get("id"), product.get("id")),
                        builder.equal(supply.get("provider").get("id"), providerId)
                        )
                );

        return builder.exists(subquery);
    }

    private Predicate existsAvailableStock(CriteriaQuery<?> query, CriteriaBuilder builder, Root<Product> product) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Storehouse> storehouse = subquery.from(Storehouse.class);

        subquery.select(builder.literal(1))
                .where(builder.and(
                        builder.equal(storehouse.get("product").get("id"), product.get("id")),
                        builder.greaterThan(storehouse.<Double>get("amount"), 0.0),
                        builder.notEqual(storehouse.get("status"), StoreStatus.SPOILED)
                        )
                );

        return builder.exists(subquery);
    }

    private Predicate existsStorehouseEntryWithStatus(CriteriaQuery<?> query, CriteriaBuilder builder, Root<Product> product, StoreStatus status) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Storehouse> storehouse = subquery.from(Storehouse.class);

        subquery.select(builder.literal(1))
                .where(builder.and(
                        builder.equal(storehouse.get("product").get("id"), product.get("id")),
                        builder.equal(storehouse.get("status"), status)),
                        builder.greaterThan(storehouse.<Double>get("amount"), 0.0)
                );

        return builder.exists(subquery);
    }

    private Predicate existsStorehouseEntryAtPlace(CriteriaQuery<?> query, CriteriaBuilder builder, Root<Product> product, Integer placeId) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Storehouse> storehouse = subquery.from(Storehouse.class);

        subquery.select(builder.literal(1))
                .where(builder.and(
                        builder.equal(storehouse.get("product").get("id"), product.get("id")),
                        builder.equal(storehouse.get("place").get("id"), placeId)),
                        builder.greaterThan(storehouse.<Double>get("amount"), 0.0)
                );

        return builder.exists(subquery);
    }

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
                                    builder.greaterThan(entry.<Double>get("amount"), 0.0),
                                    builder.notEqual(entry.get("status"), StoreStatus.SPOILED)
                            )
                    )
                    .orderBy(builder.asc(entry.get("product").get("title")));

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public List<Product> searchProduct(String title, Integer typeId, Integer providerId, Boolean inStockOnly, StoreStatus status, Integer placeId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Product> query = builder.createQuery(Product.class);
            Root<Product> product = query.from(Product.class);
            List<Predicate> predicates = new ArrayList<>();

            if (title != null && !title.isBlank()) {
                predicates.add(
                    builder.like(
                        builder.lower(product.get("title")),
                        "%" + title.trim().toLowerCase() + "%"
                    )
                );
            }

            if (typeId != null) {
                predicates.add(builder.equal(product.get("productType").get("id"), typeId));
            }

            if (providerId != null) {
                predicates.add(existsSupplyFromProvider(query, builder, product, providerId));
            }

            if (Boolean.TRUE.equals(inStockOnly)) {
                predicates.add(existsAvailableStock(query, builder, product));
            }

            if (status != null) {
                predicates.add(existsStorehouseEntryWithStatus(query, builder, product, status));
            }

            if (placeId != null) {
                predicates.add(existsStorehouseEntryAtPlace(query, builder, product, placeId));
            }

            query.select(product)
                    .distinct(true)
                    .where(predicates.toArray(Predicate[]::new))
                    .orderBy(builder.asc(product.get("title")));

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
                                    builder.greaterThan(entry.<Double>get("amount"), 0.0),
                                    builder.notEqual(entry.get("status"), StoreStatus.SPOILED)
                            )
                    );

            Double amount = session.createQuery(query).getSingleResult();
            return amount == null ? 0.0 : amount;
        }
    }

    @Override
    public long countAllProducts() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<Product> product = query.from(Product.class);

            query.select(builder.count(product));
            return session.createQuery(query).getSingleResult();
        }
    }

    @Override
    public Map<String, Long> countByType() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);

            Root<Product> product = query.from(Product.class);
            Join<Product, ProductType> type = product.join("productType");

            query.select(builder.array(
                            type.get("title"),
                            builder.count(product)
                    ))
                    .groupBy(type.get("id"), type.get("title"))
                    .orderBy(builder.asc(type.get("title")));

            List<Object[]> rows = session.createQuery(query).getResultList();

            Map<String, Long> stat = new LinkedHashMap<>();
            for (Object[] row : rows) {
                stat.put((String) row[0], ((Number) row[1]).longValue());
            }
            return stat;
        }
    }
}

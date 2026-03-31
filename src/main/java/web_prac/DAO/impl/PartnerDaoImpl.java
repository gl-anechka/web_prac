package web_prac.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import web_prac.DAO.PartnerDao;
import web_prac.model.Partner;
import web_prac.model.PartnerType;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PartnerDaoImpl extends CommonDaoImpl<Partner, Integer> implements PartnerDao {

    public PartnerDaoImpl() {
        super(Partner.class);
    }

    @Override
    public List<Partner> searchPartners(
            PartnerType type,
            String name,
            String address,
            String phone,
            String email
    ) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Partner> query = builder.createQuery(Partner.class);
            Root<Partner> partner = query.from(Partner.class);
            List<Predicate> predicates = new ArrayList<>();

            if (type == PartnerType.PROVIDER) {
                predicates.add(partner.get("type").in(PartnerType.PROVIDER, PartnerType.BOTH));
            } else if (type == PartnerType.CONSUMER) {
                predicates.add(partner.get("type").in(PartnerType.CONSUMER, PartnerType.BOTH));
            } else if (type == PartnerType.BOTH) {
                predicates.add(builder.equal(partner.get("type"), PartnerType.BOTH));
            }

            addLikePredicate(builder, partner, predicates, "name", name);
            addLikePredicate(builder, partner, predicates, "address", address);
            addLikePredicate(builder, partner, predicates, "phone", phone);
            addLikePredicate(builder, partner, predicates, "email", email);

            query.select(partner)
                    .where(predicates.toArray(Predicate[]::new))
                    .orderBy(builder.asc(partner.get("name")), builder.asc(partner.get("id")));

            return session.createQuery(query).getResultList();
        }
    }

    private void addLikePredicate(
            CriteriaBuilder builder,
            Root<Partner> partner,
            List<Predicate> predicates,
            String field,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            predicates.add(
                    builder.like(
                            builder.lower(partner.get(field)),
                            "%" + value.trim().toLowerCase() + "%"
                    )
            );
        }
    }
}

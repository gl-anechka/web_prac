package web_prac.web.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web_prac.DAO.PartnerDao;
import web_prac.model.Partner;
import web_prac.model.PartnerType;
import web_prac.web.form.PartnerForm;

import java.util.List;

@Service
public class PartnerPageService {

    private final PartnerDao partnerDao;

    @PersistenceContext
    private EntityManager entityManager;

    public PartnerPageService(PartnerDao partnerDao) {
        this.partnerDao = partnerDao;
    }

    @Transactional(readOnly = true)
    public List<Partner> search(
            PartnerType type,
            String name,
            String address,
            String phone,
            String email
    ) {
        return partnerDao.searchPartners(type, name, address, phone, email);
    }

    @Transactional(readOnly = true)
    public PartnerForm getForm(Integer id) {
        Partner partner = entityManager.find(Partner.class, id);
        if (partner == null) {
            throw new BusinessException("Партнер не найден");
        }

        PartnerForm form = new PartnerForm();
        form.setId(partner.getId());
        form.setType(partner.getType());
        form.setName(partner.getName());
        form.setAddress(partner.getAddress());
        form.setPhone(partner.getPhone());
        form.setEmail(partner.getEmail());
        return form;
    }

    @Transactional
    public void save(PartnerForm form) {
        Partner partner = form.getId() == null ? new Partner() : entityManager.find(Partner.class, form.getId());
        if (partner == null) {
            throw new BusinessException("Партнер не найден");
        }

        partner.setType(form.getType());
        partner.setName(form.getName().trim());
        partner.setAddress(normalize(form.getAddress()));
        partner.setPhone(normalize(form.getPhone()));
        partner.setEmail(normalize(form.getEmail()));

        if (form.getId() == null) {
            entityManager.persist(partner);
        }
    }

    @Transactional
    public void delete(Integer id) {
        Partner partner = entityManager.find(Partner.class, id);
        if (partner == null) {
            return;
        }

        if (hasReferences(id)) {
            throw new BusinessException("Нельзя удалить партнера, пока он участвует в операциях");
        }

        entityManager.remove(partner);
        entityManager.flush();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean hasReferences(Integer partnerId) {
        Long count = entityManager.createQuery(
                """
                select
                    (select count(s) from Supply s where s.provider.id = :partnerId) +
                    (select count(r) from Reception r where r.consumer.id = :partnerId)
                """,
                Long.class
        )
                .setParameter("partnerId", partnerId)
                .getSingleResult();

        return count != null && count > 0;
    }
}

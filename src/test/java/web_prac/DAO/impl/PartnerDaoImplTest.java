package web_prac.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import web_prac.DAO.PartnerDao;
import web_prac.model.Partner;
import web_prac.model.PartnerType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PartnerDaoImplTest extends DaoTestSupport {

    @Autowired
    private PartnerDao partnerDao;

    @Test
    void getByIdReturnsPartner() {
        assertEquals(1, partnerDao.getById(1).getId());
    }

    @Test
    void getAllReturnsAllPartners() {
        assertEquals(6, partnerDao.getAll().size());
    }

    @Test
    void savePersistsPartner() {
        Partner partner = new Partner();
        partner.setName("Northern Trade House");
        partner.setAddress("Saint Petersburg, Nevsky 15");
        partner.setPhone("79990009999");
        partner.setEmail("north.trade@example.com");
        partner.setType(PartnerType.PROVIDER);

        Partner saved = partnerDao.save(partner);

        assertNotNull(saved.getId());
        assertEquals("Northern Trade House", partnerDao.getById(saved.getId()).getName());
    }

    @Test
    void updateChangesPartner() {
        Partner partner = partnerDao.getById(1);
        partner.setName("Milk World Distribution");

        partnerDao.update(partner);

        assertEquals("Milk World Distribution", partnerDao.getById(1).getName());
    }

    @Test
    void deleteRemovesPartner() {
        Partner partner = new Partner();
        partner.setName("City Retail Group");
        partner.setType(PartnerType.CONSUMER);
        Partner saved = partnerDao.save(partner);

        partnerDao.delete(saved);

        assertNull(partnerDao.getById(saved.getId()));
    }

    @Test
    void deleteByIdRemovesPartner() {
        Partner partner = new Partner();
        partner.setName("Volga Supplies");
        partner.setType(PartnerType.PROVIDER);
        Partner saved = partnerDao.save(partner);

        partnerDao.deleteById(saved.getId());

        assertNull(partnerDao.getById(saved.getId()));
    }

    @Test
    void searchPartnersFiltersByTypeAndFields() {
        List<Partner> partners = partnerDao.searchPartners(
                PartnerType.PROVIDER,
                null,
                null,
                null,
                "ut@"
        );

        assertEquals(1, partners.size());
        assertEquals(6, partners.get(0).getId());
    }
}

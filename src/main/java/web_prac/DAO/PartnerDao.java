package web_prac.DAO;

import java.util.List;
import web_prac.model.Partner;
import web_prac.model.PartnerType;

public interface PartnerDao extends CommonDao<Partner, Integer> {

    List<Partner> searchPartners(
            PartnerType type,
            String name,
            String address,
            String phone,
            String email
    );
}

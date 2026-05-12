package web_prac.web.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import web_prac.DAO.dto.OperationKind;
import web_prac.model.PartnerType;
import web_prac.model.StoreStatus;
import web_prac.model.Unit;

@ControllerAdvice
public class CommonViewModelAdvice {
    @ModelAttribute("partnerTypes")
    public PartnerType[] partnerTypes() {
        return PartnerType.values();
    }

    @ModelAttribute("storeStatuses")
    public StoreStatus[] storeStatuses() {
        return StoreStatus.values();
    }

    @ModelAttribute("units")
    public Unit[] units() {
        return Unit.values();
    }

    @ModelAttribute("operationKinds")
    public OperationKind[] operationKinds() {
        return OperationKind.values();
    }
}

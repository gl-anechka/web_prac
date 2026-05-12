package web_prac.web.service;

import org.springframework.stereotype.Service;
import web_prac.DAO.dto.OperationKind;
import web_prac.model.PartnerType;
import web_prac.model.StoreStatus;
import web_prac.model.Unit;

@Service("displayValueService")
public class DisplayValueService {

    public String partnerType(PartnerType type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case PROVIDER -> "Поставщик";
            case CONSUMER -> "Потребитель";
            case BOTH -> "Поставщик и потребитель";
        };
    }

    public String operationKind(OperationKind kind) {
        if (kind == null) {
            return "";
        }

        return kind == OperationKind.SUPPLY ? "Поставка" : "Выдача";
    }

    public String storeStatus(StoreStatus status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case OK -> "Хороший товар";
            case NEAR_EXPIRY -> "Скоро истекает срок годности";
            case SPOILED -> "Испорчен";
        };
    }

    public String unit(Unit unit) {
        if (unit == null) {
            return "";
        }

        return switch (unit) {
            case KG -> "кг";
            case PCS -> "шт";
            case L -> "л";
        };
    }
}

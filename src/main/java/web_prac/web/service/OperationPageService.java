package web_prac.web.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web_prac.DAO.OperationDao;
import web_prac.DAO.PlaceDao;
import web_prac.DAO.ProductDao;
import web_prac.DAO.dto.OperationKind;
import web_prac.DAO.dto.OperationView;
import web_prac.model.Partner;
import web_prac.model.PartnerType;
import web_prac.model.Place;
import web_prac.model.Product;
import web_prac.model.Reception;
import web_prac.model.StoreStatus;
import web_prac.model.Storehouse;
import web_prac.model.Supply;
import web_prac.web.form.OperationForm;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationPageService {

    private final OperationDao operationDao;
    private final PlaceDao placeDao;
    private final ProductDao productDao;
    private final ReferenceDataService referenceDataService;

    @PersistenceContext
    private EntityManager entityManager;

    public OperationPageService(
            OperationDao operationDao,
            PlaceDao placeDao,
            ProductDao productDao,
            ReferenceDataService referenceDataService
    ) {
        this.operationDao = operationDao;
        this.placeDao = placeDao;
        this.productDao = productDao;
        this.referenceDataService = referenceDataService;
    }

    @Transactional(readOnly = true)
    public List<OperationView> search(
            OperationKind kind,
            LocalDateTime from,
            LocalDateTime to,
            Integer partnerId,
            Integer productId
    ) {
        return operationDao.findByFilter(kind, from, to, partnerId, productId);
    }

    @Transactional(readOnly = true)
    public OperationForm createForm(OperationKind kind) {
        OperationForm form = new OperationForm();
        form.setKind(kind);
        form.setTime(LocalDateTime.now().withSecond(0).withNano(0));
        return form;
    }

    @Transactional(readOnly = true)
    public String checkSupplyAvailability(OperationForm form) {
        if (form.getKind() != OperationKind.SUPPLY) {
            throw new BusinessException("Проверка места доступна только для поставки");
        }

        Product product = requireProduct(form.getProductId());
        validatePositiveAmount(form.getAmount());

        Place place = placeDao.findFirstSuitablePlace(product.getId(), form.getAmount());
        if (place == null) {
            throw new BusinessException("На складе недостаточно свободного места для этой поставки");
        }

        return referenceDataService.formatPlace(place);
    }

    @Transactional
    public void save(OperationForm form) {
        if (form.getKind() == OperationKind.SUPPLY) {
            saveSupply(form);
            return;
        }

        if (form.getKind() == OperationKind.RECEPTION) {
            saveReception(form);
            return;
        }

        throw new BusinessException("Неизвестный тип операции");
    }

    private void saveSupply(OperationForm form) {
        Partner provider = requirePartner(form.getPartnerId(), PartnerType.PROVIDER);
        Product product = requireProduct(form.getProductId());
        validatePositiveAmount(form.getAmount());

        Place place = placeDao.findFirstSuitablePlace(product.getId(), form.getAmount());
        if (place == null) {
            throw new BusinessException("На складе недостаточно свободного места для этой поставки");
        }

        Supply supply = new Supply();
        supply.setProvider(provider);
        supply.setProduct(product);
        supply.setTime(form.getTime());
        supply.setAmount(form.getAmount());
        entityManager.persist(supply);

        Storehouse storehouse = new Storehouse();
        storehouse.setProduct(product);
        storehouse.setAmount(form.getAmount());
        storehouse.setPlace(entityManager.getReference(Place.class, place.getId()));
        storehouse.setSupply(supply);
        storehouse.setReceivedAt(form.getTime());
        storehouse.setExpiresAt(form.getExpiresAt());
        storehouse.setStatus(StoreStatus.OK);
        entityManager.persist(storehouse);
    }

    private void saveReception(OperationForm form) {
        Partner consumer = requirePartner(form.getPartnerId(), PartnerType.CONSUMER);
        Product product = requireProduct(form.getProductId());
        validatePositiveAmount(form.getAmount());

        double availableAmount = productDao.getAvailableAmount(product.getId());
        if (availableAmount < form.getAmount()) {
            throw new BusinessException("На складе недостаточно товара для выдачи");
        }

        Reception reception = new Reception();
        reception.setConsumer(consumer);
        reception.setProduct(product);
        reception.setTime(form.getTime());
        reception.setAmount(form.getAmount());
        reception.setCompleted(true);
        entityManager.persist(reception);

        double remaining = form.getAmount();
        for (Storehouse item : loadStockEntries(product.getId())) {
            if (remaining <= 0) {
                break;
            }

            double taken = Math.min(item.getAmount(), remaining);
            item.setAmount(item.getAmount() - taken);
            item.setReception(reception);
            remaining -= taken;
        }
    }

    private List<Storehouse> loadStockEntries(Integer productId) {
        return entityManager.createQuery(
                """
                select s
                from Storehouse s
                where s.product.id = :productId
                  and s.amount > 0
                  and s.status <> :spoiled
                order by case when s.expiresAt is null then 1 else 0 end,
                         s.expiresAt,
                         s.receivedAt,
                         s.id
                """,
                Storehouse.class
        )
                .setParameter("productId", productId)
                .setParameter("spoiled", StoreStatus.SPOILED)
                .getResultList();
    }

    private Partner requirePartner(Integer partnerId, PartnerType expectedRole) {
        Partner partner = entityManager.find(Partner.class, partnerId);
        if (partner == null) {
            throw new BusinessException("Партнер не найден");
        }

        boolean allowed = switch (expectedRole) {
            case PROVIDER -> partner.getType() == PartnerType.PROVIDER || partner.getType() == PartnerType.BOTH;
            case CONSUMER -> partner.getType() == PartnerType.CONSUMER || partner.getType() == PartnerType.BOTH;
            case BOTH -> true;
        };

        if (!allowed) {
            throw new BusinessException(
                    expectedRole == PartnerType.PROVIDER
                            ? "Выбранный партнер не может быть поставщиком"
                            : "Выбранный партнер не может быть получателем"
            );
        }

        return partner;
    }

    private Product requireProduct(Integer productId) {
        Product product = entityManager.find(Product.class, productId);
        if (product == null) {
            throw new BusinessException("Товар не найден");
        }
        return product;
    }

    private void validatePositiveAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException("Количество должно быть больше нуля");
        }
    }
}

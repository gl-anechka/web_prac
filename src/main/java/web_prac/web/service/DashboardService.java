package web_prac.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web_prac.DAO.OperationDao;
import web_prac.DAO.ProductDao;
import web_prac.web.view.DashboardView;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductDao productDao;
    private final OperationDao operationDao;
    private final ProductPresentationService productPresentationService;

    public DashboardService(
            ProductDao productDao,
            OperationDao operationDao,
            ProductPresentationService productPresentationService
    ) {
        this.productDao = productDao;
        this.operationDao = operationDao;
        this.productPresentationService = productPresentationService;
    }

    public DashboardView getDashboard() {
        return new DashboardView(
                productDao.countAllProducts(),
                productDao.countByType(),
                productPresentationService.toRows(productDao.findExpiringBefore(LocalDateTime.now().plusDays(30))),
                operationDao.findRecent(10)
        );
    }
}

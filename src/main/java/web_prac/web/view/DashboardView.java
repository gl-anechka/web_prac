package web_prac.web.view;

import web_prac.DAO.dto.OperationView;

import java.util.List;
import java.util.Map;

//данные главной страницы
public record DashboardView(
    long totalProducts,
    Map<String, Long> countByType,
    List<ProductRowView> expiringProducts,
    List<OperationView> recentOperations
) {
}

package web_prac.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import web_prac.web.service.DashboardService;

@Controller
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("dashboard", dashboardService.getDashboard());
        return "home";
    }
}

package web_prac.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SectionController {

    @GetMapping("/partners")
    public String partners(Model model) {
        return placeholder(
                model,
                "partners",
                "Партнеры",
                "Раздел партнеров еще не реализован.",
                "Здесь должны быть поиск, список контрагентов и форма добавления или редактирования."
        );
    }

    @GetMapping("/operations")
    public String operations(Model model) {
        return placeholder(
                model,
                "operations",
                "Операции",
                "Раздел операций еще не реализован.",
                "Здесь должны быть журнал операций, фильтры и оформление поставки или выдачи."
        );
    }

    @GetMapping("/places")
    public String places(Model model) {
        return placeholder(
                model,
                "places",
                "Места хранения",
                "Раздел мест хранения еще не реализован.",
                "Здесь должны быть список полок и форма добавления или редактирования места."
        );
    }

    private String placeholder(
            Model model,
            String activeSection,
            String pageTitle,
            String lead,
            String details
    ) {
        model.addAttribute("activeSection", activeSection);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("lead", lead);
        model.addAttribute("details", details);
        return "sections/placeholder";
    }
}

package web_prac.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import web_prac.model.PartnerType;
import web_prac.web.form.PartnerForm;
import web_prac.web.service.BusinessException;
import web_prac.web.service.PartnerPageService;

@Controller
@RequestMapping("/partners")
public class PartnerController {

    private final PartnerPageService partnerPageService;

    public PartnerController(PartnerPageService partnerPageService) {
        this.partnerPageService = partnerPageService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) PartnerType type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            Model model
    ) {
        model.addAttribute("partners", partnerPageService.search(type, name, address, phone, email));
        model.addAttribute("type", type);
        model.addAttribute("name", name);
        model.addAttribute("address", address);
        model.addAttribute("phone", phone);
        model.addAttribute("email", email);
        return "partners/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("partnerForm", new PartnerForm());
        model.addAttribute("pageTitle", "Зарегистрировать партнера");
        return "partners/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("partnerForm", partnerPageService.getForm(id));
            model.addAttribute("pageTitle", "Изменить партнера");
            return "partners/form";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/partners";
        }
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("partnerForm") PartnerForm partnerForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    partnerForm.getId() == null ? "Зарегистрировать партнера" : "Изменить партнера"
            );
            return "partners/form";
        }

        try {
            partnerPageService.save(partnerForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    partnerForm.getId() == null
                            ? "Партнер успешно зарегистрирован"
                            : "Данные партнера обновлены"
            );
            return "redirect:/partners";
        } catch (BusinessException e) {
            model.addAttribute(
                    "pageTitle",
                    partnerForm.getId() == null ? "Зарегистрировать партнера" : "Изменить партнера"
            );
            model.addAttribute("errorMessage", e.getMessage());
            return "partners/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            partnerPageService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Партнер удален");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/partners";
    }
}

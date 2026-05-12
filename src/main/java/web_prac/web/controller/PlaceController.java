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
import web_prac.web.form.PlaceForm;
import web_prac.web.service.BusinessException;
import web_prac.web.service.PlacePageService;

@Controller
@RequestMapping("/places")
public class PlaceController {

    private final PlacePageService placePageService;

    public PlaceController(PlacePageService placePageService) {
        this.placePageService = placePageService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) Integer roomNum,
            @RequestParam(required = false) Integer shelfNum,
            @RequestParam(required = false) Double minFreeCapacity,
            Model model
    ) {
        model.addAttribute("places", placePageService.search(roomNum, shelfNum, minFreeCapacity));
        model.addAttribute("roomNum", roomNum);
        model.addAttribute("shelfNum", shelfNum);
        model.addAttribute("minFreeCapacity", minFreeCapacity);
        return "places/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("placeForm", new PlaceForm());
        model.addAttribute("pageTitle", "Добавить место хранения");
        return "places/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("placeForm", placePageService.getForm(id));
            model.addAttribute("pageTitle", "Изменить место хранения");
            return "places/form";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/places";
        }
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("placeForm") PlaceForm placeForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    placeForm.getId() == null ? "Добавить место хранения" : "Изменить место хранения"
            );
            return "places/form";
        }

        try {
            placePageService.save(placeForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    placeForm.getId() == null
                            ? "Место хранения успешно добавлено"
                            : "Место хранения успешно обновлено"
            );
            return "redirect:/places";
        } catch (BusinessException e) {
            model.addAttribute(
                    "pageTitle",
                    placeForm.getId() == null ? "Добавить место хранения" : "Изменить место хранения"
            );
            model.addAttribute("errorMessage", e.getMessage());
            return "places/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            placePageService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Место хранения удалено");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/places";
    }
}

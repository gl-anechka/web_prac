package web_prac.web.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import web_prac.DAO.dto.OperationKind;
import web_prac.web.form.OperationForm;
import web_prac.web.service.BusinessException;
import web_prac.web.service.OperationPageService;
import web_prac.web.service.ReferenceDataService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/operations")
public class OperationController {

    private final OperationPageService operationPageService;
    private final ReferenceDataService referenceDataService;

    public OperationController(
            OperationPageService operationPageService,
            ReferenceDataService referenceDataService
    ) {
        this.operationPageService = operationPageService;
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) OperationKind kind,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to,
            @RequestParam(required = false) Integer partnerId,
            @RequestParam(required = false) Integer productId,
            Model model
    ) {
        model.addAttribute("operations", operationPageService.search(kind, from, to, partnerId, productId));
        model.addAttribute("kind", kind);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("partnerId", partnerId);
        model.addAttribute("productId", productId);
        model.addAttribute("partnerOptions", referenceDataService.getPartnerOptions(null));
        model.addAttribute("productOptions", referenceDataService.getProductOptions());
        return "operations/list";
    }

    @GetMapping("/new-supply")
    public String createSupplyForm(Model model) {
        return operationForm(model, operationPageService.createForm(OperationKind.SUPPLY), null, null);
    }

    @GetMapping("/new-reception")
    public String createReceptionForm(Model model) {
        return operationForm(model, operationPageService.createForm(OperationKind.RECEPTION), null, null);
    }

    @PostMapping("/check-place")
    public String checkPlace(
            @Valid @ModelAttribute("operationForm") OperationForm operationForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return operationForm(model, operationForm, null, null);
        }

        try {
            return operationForm(
                    model,
                    operationForm,
                    "Свободное место найдено: " + operationPageService.checkSupplyAvailability(operationForm),
                    null
            );
        } catch (BusinessException e) {
            return operationForm(model, operationForm, null, e.getMessage());
        }
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("operationForm") OperationForm operationForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return operationForm(model, operationForm, null, null);
        }

        try {
            operationPageService.save(operationForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    operationForm.getKind() == OperationKind.SUPPLY
                            ? "Поставка успешно оформлена"
                            : "Выдача успешно оформлена"
            );
            return "redirect:/operations";
        } catch (BusinessException e) {
            return operationForm(model, operationForm, null, e.getMessage());
        }
    }

    private String operationForm(
            Model model,
            OperationForm operationForm,
            String successMessage,
            String errorMessage
    ) {
        if (!model.containsAttribute("operationForm")) {
            model.addAttribute("operationForm", operationForm);
        }
        model.addAttribute(
                "pageTitle",
                operationForm.getKind() == OperationKind.RECEPTION ? "Оформить выдачу" : "Оформить поставку"
        );
        model.addAttribute(
                "partnerOptions",
                operationForm.getKind() == OperationKind.RECEPTION
                        ? referenceDataService.getPartnerOptionsForConsumers()
                        : referenceDataService.getPartnerOptionsForProviders()
        );
        model.addAttribute("productOptions", referenceDataService.getProductOptions());
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "operations/form";
    }
}

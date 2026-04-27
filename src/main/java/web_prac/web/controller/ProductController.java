package web_prac.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import web_prac.model.StoreStatus;
import web_prac.web.form.ProductForm;
import web_prac.web.service.BusinessException;
import web_prac.web.service.ProductPageService;
import web_prac.web.service.ReferenceDataService;

@Controller
@RequestMapping("/products")
//веб страницы, связанные с товарами
public class ProductController {
    private final ProductPageService productPageService;
    private final ReferenceDataService referenceDataService;

    private ProductController(ProductPageService productPageService, ReferenceDataService referenceDataService) {
        this.productPageService = productPageService;
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public String list(
     @RequestParam(required = false) String title,
     @RequestParam(required = false) Integer typeId,
     @RequestParam(required = false) Boolean inStockOnly,
     @RequestParam(required = false)StoreStatus status,
     Model model
     ) {
        model.addAttribute("products", productPageService.search(title, typeId, inStockOnly, status));
        model.addAttribute("title", title);
        model.addAttribute("typeId", typeId);
        model.addAttribute("inStockOnly", Boolean.TRUE.equals(inStockOnly));
        model.addAttribute("status", status);

        fillReferenceData(model);
        return "products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("pageTitle", "Добавить товар");

        fillReferenceData(model);
        return "products/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("productForm", productPageService.getForm(id));
            model.addAttribute("pageTitle", "Изменить товар");
            fillReferenceData(model);
            return "products/form";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("productForm") ProductForm productForm,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", productForm.getId() == null ? "Добавить товар" : "Изменить товар");
            fillReferenceData(model);
            return "products/form";
        }

        try {
            productPageService.save(productForm);
            redirectAttributes.addFlashAttribute("successMessage",
                                    productForm.getId() == null ? "Товар успешно добавлен" : "Товар успешно обновлен");
            return "redirect:/products";
        } catch (RuntimeException e) {
            model.addAttribute("pageTitle", productForm.getId() == null ? "Добавить товар" : "Изменить товар");
            model.addAttribute("errorMessage", e.getMessage());
            fillReferenceData(model);
            return "products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes
    ) {
        productPageService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Товар удален");
        return "redirect:/products";
    }

    private void fillReferenceData(Model model) {
        model.addAttribute("productTypesList", referenceDataService.getProductTypes());
    }
}

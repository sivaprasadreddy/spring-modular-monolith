package com.sivalabs.bookstore.inventory;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/inventory")
class AdminInventoryWebController {
    private static final Logger log = LoggerFactory.getLogger(AdminInventoryWebController.class);

    private final InventoryService inventoryService;

    AdminInventoryWebController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    String showInventory(@RequestParam(defaultValue = "1") int page, Model model, HtmxRequest hxRequest) {
        log.info("Admin fetching inventory for page: {}", page);
        model.addAttribute("inventoryPage", inventoryService.getAllInventory(page));
        if (hxRequest.isHtmxRequest()) {
            return "partials/admin/inventory";
        }
        return "admin/inventory";
    }

    @PostMapping("/{productCode}")
    String updateStockLevel(@PathVariable String productCode, @RequestParam long quantity) {
        log.info("Admin updating stock level for productCode: {}, quantity: {}", productCode, quantity);
        inventoryService.updateStockLevel(productCode, quantity);
        return "redirect:/admin/inventory";
    }
}

package com.sivalabs.bookstore.inventory;

import com.sivalabs.bookstore.common.models.PagedResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
class AdminInventoryRestController {
    private static final Logger log = LoggerFactory.getLogger(AdminInventoryRestController.class);

    private final InventoryService inventoryService;

    AdminInventoryRestController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    PagedResult<InventoryView> getInventory(@RequestParam(defaultValue = "1") int page) {
        log.info("Admin fetching inventory for page: {}", page);
        return inventoryService.getAllInventory(page);
    }

    @PutMapping("/{productCode}")
    ResponseEntity<InventoryView> updateStockLevel(
            @PathVariable String productCode, @Valid @RequestBody UpdateStockLevelRequest request) {
        log.info("Admin updating stock level for productCode: {}, quantity: {}", productCode, request.quantity());
        InventoryView updated = inventoryService.updateStockLevel(productCode, request.quantity());
        return ResponseEntity.ok(updated);
    }
}

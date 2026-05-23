package com.sivalabs.bookstore.inventory;

import com.sivalabs.bookstore.common.models.PagedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final int PAGE_SIZE = 10;

    private final InventoryRepository inventoryRepository;

    InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public void decreaseStockLevel(String productCode, int quantity) {
        log.info("Decrease stock level for product code {} and quantity {}", productCode, quantity);
        var inventory = inventoryRepository.findByProductCode(productCode).orElse(null);
        if (inventory != null) {
            long newQuantity = inventory.getQuantity() - quantity;
            inventory.setQuantity(newQuantity);
            inventoryRepository.save(inventory);
            log.info("Updated stock level for product code {} to : {}", productCode, newQuantity);
        } else {
            log.warn("Invalid product code {}", productCode);
        }
    }

    @Transactional(readOnly = true)
    public PagedResult<InventoryView> getAllInventory(int pageNo) {
        var pageable =
                PageRequest.of(pageNo - 1, PAGE_SIZE, Sort.by("productCode").ascending());
        Page<InventoryView> page =
                inventoryRepository.findAll(pageable).map(e -> new InventoryView(e.getProductCode(), e.getQuantity()));
        return new PagedResult<>(page);
    }

    @Transactional
    public InventoryView updateStockLevel(String productCode, long quantity) {
        if (quantity < 0) {
            throw InvalidInventoryException.negativeQuantity(quantity);
        }
        InventoryEntity entity = inventoryRepository
                .findByProductCode(productCode)
                .orElseGet(() -> {
                    InventoryEntity e = new InventoryEntity();
                    e.setProductCode(productCode);
                    return e;
                });
        entity.setQuantity(quantity);
        InventoryEntity saved = inventoryRepository.save(entity);
        log.info("Updated stock level for product code {} to : {}", productCode, quantity);
        return new InventoryView(saved.getProductCode(), saved.getQuantity());
    }

    @Transactional(readOnly = true)
    public Long getStockLevel(String productCode) {
        Long stock = inventoryRepository
                .findByProductCode(productCode)
                .map(InventoryEntity::getQuantity)
                .orElse(0L);
        log.info("Stock level for product code {} is : {}", productCode, stock);
        return stock;
    }
}

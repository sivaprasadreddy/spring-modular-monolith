package com.sivalabs.bookstore.inventory.internal;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final Map<String, Long> STOCK = new HashMap<>();

    @PostConstruct
    void init() {
        STOCK.put("P100", 100L);
        STOCK.put("P101", 50L);
        STOCK.put("P102", 500L);
    }

    public void decreaseStockLevel(String productCode, int quantity) {
        log.info("Decrease stock level for product code {} and quantity {}", productCode, quantity);
        if (STOCK.containsKey(productCode)) {
            STOCK.put(productCode, STOCK.get(productCode) - quantity);
        }
        System.out.println(STOCK);
    }

    public Long getStockLevel(String productCode) {
        return STOCK.get(productCode);
    }
}

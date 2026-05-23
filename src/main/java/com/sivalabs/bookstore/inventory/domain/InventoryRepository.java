package com.sivalabs.bookstore.inventory.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    Optional<InventoryEntity> findByProductCode(String productCode);
}

package com.sivalabs.bookstore.catalog.domain;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByCode(String code);

    boolean existsByCode(String code);

    Optional<ProductEntity> findByCodeAndDeletedAtIsNull(String code);

    Page<ProductEntity> findAllByDeletedAtIsNull(Pageable pageable);
}

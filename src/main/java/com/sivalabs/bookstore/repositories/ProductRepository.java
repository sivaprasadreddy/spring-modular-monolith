package com.sivalabs.bookstore.repositories;

import com.sivalabs.bookstore.entities.ProductEntity;
import com.sivalabs.bookstore.models.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query(
            """
        select new com.sivalabs.bookstore.models.Product(
                p.code, p.name, p.description, p.imageUrl, p.price)
        from ProductEntity p
        """)
    Page<Product> findAllBy(Pageable pageable);

    @Query(
            """
        select new com.sivalabs.bookstore.models.Product(
                p.code, p.name, p.description, p.imageUrl, p.price)
        from ProductEntity p
        where p.code = :code
        """)
    Optional<Product> findByCode(String code);
}

package com.sivalabs.bookstore.catalog.domain;

import static jakarta.persistence.GenerationType.SEQUENCE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "products", schema = "catalog")
class ProductEntity {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "product_id_generator")
    @SequenceGenerator(name = "product_id_generator", sequenceName = "product_id_seq", schema = "catalog")
    private Long id = 0L;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "Product code is required") private String code = "";

    @NotEmpty(message = "Product name is required") @Column(nullable = false)
    private String name = "";

    @Nullable private String description;

    @Nullable private String imageUrl;

    @NotNull(message = "Product price is required") @DecimalMin("0.1") @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Nullable @Column(name = "deleted_at")
    private Instant deletedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@Nullable String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public @Nullable Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(@Nullable Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}

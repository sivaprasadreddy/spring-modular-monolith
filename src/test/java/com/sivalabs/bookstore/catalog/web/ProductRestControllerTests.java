package com.sivalabs.bookstore.catalog.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.catalog.ProductDto;
import com.sivalabs.bookstore.common.models.PagedResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ApplicationModuleTest(
        webEnvironment = RANDOM_PORT,
        extraIncludes = {"config", "users"})
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Sql("/test-products-data.sql")
class ProductRestControllerTests {
    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    void shouldGetProducts() {
        assertThat(mockMvcTester.get().uri("/api/products"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(paged -> {
                    PagedResult<?> pr = (PagedResult<?>) paged;
                    assertThat(pr.totalElements()).isEqualTo(15);
                    assertThat(pr.pageNumber()).isEqualTo(1);
                    assertThat(pr.totalPages()).isEqualTo(2);
                    assertThat(pr.isFirst()).isTrue();
                    assertThat(pr.isLast()).isFalse();
                    assertThat(pr.hasNext()).isTrue();
                    assertThat(pr.hasPrevious()).isFalse();
                    assertThat(pr.data()).isNotNull();
                });
    }

    @Test
    void shouldGetProductByCode() {
        assertThat(mockMvcTester.get().uri("/api/products/{code}", "P100"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(ProductDto.class)
                .satisfies(product -> {
                    assertThat(product.code()).isEqualTo("P100");
                    assertThat(product.name()).isEqualTo("The Hunger Games");
                });
    }
}

package com.sivalabs.bookstore.catalog.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.common.models.PagedResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ApplicationModuleTest(
        webEnvironment = RANDOM_PORT,
        extraIncludes = {"config", "users"})
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Sql("/test-products-data.sql")
class AdminProductRestControllerTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    void shouldReturn200WithProductsForAdminUser() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/catalog/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(paged -> {
                    PagedResult<?> pr = (PagedResult<?>) paged;
                    assertThat(pr.totalElements()).isEqualTo(15);
                    assertThat(pr.pageNumber()).isEqualTo(1);
                    assertThat(pr.totalPages()).isEqualTo(2);
                    assertThat(pr.isFirst()).isTrue();
                    assertThat(pr.hasNext()).isTrue();
                    assertThat(pr.data()).isNotEmpty();
                });
    }

    @Test
    void shouldReturn403ForNonAdminUser() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/catalog/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        assertThat(mockMvcTester.get().uri("/api/admin/catalog/products")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnCorrectPage2Results() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/catalog/products?page=2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(paged -> {
                    PagedResult<?> pr = (PagedResult<?>) paged;
                    assertThat(pr.pageNumber()).isEqualTo(2);
                    assertThat(pr.isLast()).isTrue();
                    assertThat(pr.hasPrevious()).isTrue();
                    assertThat(pr.hasNext()).isFalse();
                });
    }
}

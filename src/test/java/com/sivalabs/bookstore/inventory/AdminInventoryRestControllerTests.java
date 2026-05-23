package com.sivalabs.bookstore.inventory;

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
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ApplicationModuleTest(
        webEnvironment = RANDOM_PORT,
        extraIncludes = {"config", "users"})
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class AdminInventoryRestControllerTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    void shouldReturn200WithInventoryForAdminUser() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/inventory")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(paged -> {
                    PagedResult<?> pr = (PagedResult<?>) paged;
                    assertThat(pr.totalElements()).isGreaterThan(0);
                    assertThat(pr.data()).isNotEmpty();
                });
    }

    @Test
    void shouldReturnFirstPageOfInventory() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/inventory?page=1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(paged -> {
                    PagedResult<?> pr = (PagedResult<?>) paged;
                    assertThat(pr.pageNumber()).isEqualTo(1);
                    assertThat(pr.data()).isNotEmpty();
                });
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        assertThat(mockMvcTester.get().uri("/api/admin/inventory")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403ForNonAdminUser() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/inventory")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldUpdateStockLevelForExistingProduct() {
        assertThat(mockMvcTester
                        .put()
                        .uri("/api/admin/inventory/P100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":500}
                                """)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(InventoryView.class)
                .satisfies(view -> {
                    assertThat(view.productCode()).isEqualTo("P100");
                    assertThat(view.quantity()).isEqualTo(500L);
                });
    }

    @Test
    void shouldCreateInventoryRecordForNewProductCode() {
        assertThat(mockMvcTester
                        .put()
                        .uri("/api/admin/inventory/P999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":100}
                                """)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(InventoryView.class)
                .satisfies(view -> {
                    assertThat(view.productCode()).isEqualTo("P999");
                    assertThat(view.quantity()).isEqualTo(100L);
                });
    }

    @Test
    void shouldReturn400ForNegativeQuantity() {
        assertThat(mockMvcTester
                        .put()
                        .uri("/api/admin/inventory/P100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":-1}
                                """)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn401WhenNoTokenProvidedOnUpdate() {
        assertThat(mockMvcTester
                        .put()
                        .uri("/api/admin/inventory/P100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":100}
                                """))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403ForNonAdminUserOnUpdate() {
        assertThat(mockMvcTester
                        .put()
                        .uri("/api/admin/inventory/P100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":100}
                                """)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .hasStatus(HttpStatus.FORBIDDEN);
    }
}

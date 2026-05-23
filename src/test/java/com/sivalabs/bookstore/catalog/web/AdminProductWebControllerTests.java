package com.sivalabs.bookstore.catalog.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class AdminProductWebControllerTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    void shouldRenderProductDetailPageForValidCode() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/catalog/products/P100")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("The Hunger Games");
    }

    @Test
    void shouldIncludeBackLinkOnDetailPage() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/catalog/products/P100")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("/admin/catalog/products");
    }

    @Test
    void shouldReturnPartialFragmentForHtmxRequest() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/catalog/products/P100")
                        .header("HX-Request", "true")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .doesNotContain("admin-topbar");
    }

    @Test
    void shouldLinkProductNameInListToDetailPage() {
        // P111 (A Game of Thrones) sorts first alphabetically and appears on page 1
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/catalog/products")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("/admin/catalog/products/P111");
    }

    @Test
    void shouldReturn404ForNonExistentProductCode() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/catalog/products/INVALID")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRenderCreateProductForm() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/catalog/products/new")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("New Product");
    }

    @Test
    void shouldCreateProductAndRedirectToDetailPage() {
        assertThat(mockMvcTester
                        .post()
                        .uri("/admin/catalog/products")
                        .param("code", "PTEST")
                        .param("name", "Test Book")
                        .param("price", "19.99")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .hasStatus(HttpStatus.FOUND)
                .hasHeader("Location", "/admin/catalog/products/PTEST");
    }

    @Test
    void shouldReRenderFormOnValidationError() {
        assertThat(mockMvcTester
                        .post()
                        .uri("/admin/catalog/products")
                        .param("code", "")
                        .param("name", "")
                        .param("price", "")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Product code is required");
    }

    @Test
    void shouldReRenderFormOnDuplicateCode() {
        assertThat(mockMvcTester
                        .post()
                        .uri("/admin/catalog/products")
                        .param("code", "P100")
                        .param("name", "Duplicate Book")
                        .param("price", "19.99")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("A product with this code already exists.");
    }

    @Test
    void shouldShowNewProductButtonOnListPage() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/catalog/products")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("/admin/catalog/products/new");
    }
}

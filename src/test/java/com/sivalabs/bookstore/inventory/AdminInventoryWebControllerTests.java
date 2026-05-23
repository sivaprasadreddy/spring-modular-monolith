package com.sivalabs.bookstore.inventory;

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
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ApplicationModuleTest(
        webEnvironment = RANDOM_PORT,
        extraIncludes = {"config", "users"})
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class AdminInventoryWebControllerTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    void shouldRenderInventoryPage() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/inventory")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Stock Levels");
    }

    @Test
    void shouldShowInventoryRecordsInList() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/inventory")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("P100");
    }

    @Test
    void shouldShowQuantityColumnInList() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/inventory")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Quantity");
    }

    @Test
    void shouldReturnPartialFragmentForHtmxRequest() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/inventory")
                        .header("HX-Request", "true")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .doesNotContain("admin-topbar");
    }

    @Test
    void shouldShowUpdateStockFormForEachRow() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/inventory")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Update Stock")
                .contains("/admin/inventory/P100");
    }

    @Test
    void shouldUpdateStockLevelAndRedirectToInventoryPage() {
        assertThat(mockMvcTester
                        .post()
                        .uri("/admin/inventory/P100")
                        .param("quantity", "999")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.FOUND)
                .hasHeader("Location", "/admin/inventory");
    }

    @Test
    void shouldReflectUpdatedQuantityInInventoryList() {
        mockMvcTester
                .post()
                .uri("/admin/inventory/P100")
                .param("quantity", "777")
                .with(csrf())
                .with(user("admin").roles("ADMIN"))
                .exchange();

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/inventory")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("777");
    }
}

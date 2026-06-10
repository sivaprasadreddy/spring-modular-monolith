package com.sivalabs.bookstore.orders.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.catalog.ProductApi;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderCmd;
import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import com.sivalabs.bookstore.orders.domain.models.OrderStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ApplicationModuleTest(
        webEnvironment = RANDOM_PORT,
        extraIncludes = {"config", "users"})
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class AdminOrderWebControllerTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    ProductApi productApi;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM orders.orders");
        orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));
        orderService.createOrder(buildCreateOrderCmd(2L, "Bob Jones", "bob@example.com"));
    }

    @Test
    void shouldRenderOrderListPage() {
        assertThat(mockMvcTester.get().uri("/admin/orders").with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Orders");
    }

    @Test
    void shouldShowOrdersInList() {
        assertThat(mockMvcTester.get().uri("/admin/orders").with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Alice Smith");
    }

    @Test
    void shouldShowStatusColumnInList() {
        assertThat(mockMvcTester.get().uri("/admin/orders").with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("NEW");
    }

    @Test
    void shouldShowStatusFilterDropdown() {
        assertThat(mockMvcTester.get().uri("/admin/orders").with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("All Statuses");
    }

    @Test
    void shouldFilterByStatusAndReturnMatchingOrders() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders?status=NEW")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Alice Smith");
    }

    @Test
    void shouldReturnEmptyTableForStatusWithNoOrders() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders?status=DELIVERED")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("No orders found.");
    }

    @Test
    void shouldReturnPartialFragmentForHtmxRequest() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders")
                        .header("HX-Request", "true")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .doesNotContain("admin-topbar");
    }

    @Test
    void shouldRenderOrderDetailPage() {
        var result = orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", result.orderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Alice Smith");
    }

    @Test
    void shouldShowOrderStatusOnDetailPage() {
        var result = orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", result.orderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("NEW");
    }

    @Test
    void shouldReturn404ForNonExistentOrderNumber() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/non-existent-order")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnPartialFragmentForHtmxRequestOnDetail() {
        var result = orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", result.orderNumber())
                        .header("HX-Request", "true")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .doesNotContain("admin-topbar");
    }

    @Test
    void shouldLinkOrderNumberInListToDetailPage() {
        var result = orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester.get().uri("/admin/orders").with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("/admin/orders/" + result.orderNumber());
    }

    @Test
    void shouldUpdateOrderStatusAndRedirectToDetailPage() {
        var result = orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .post()
                        .uri("/admin/orders/{orderNumber}/status", result.orderNumber())
                        .param("status", "IN_PROCESS")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.FOUND)
                .hasHeader("Location", "/admin/orders/" + result.orderNumber());
    }

    @Test
    void shouldShowMarkAsInProcessButtonForNewOrder() {
        var result = orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", result.orderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Mark as In Process");
    }

    @Test
    void shouldShowCancelButtonForNewOrder() {
        var result = orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", result.orderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Cancel Order");
    }

    @Test
    void shouldNotShowTransitionButtonsForDeliveredOrder() {
        var result = orderService.createOrder(buildCreateOrderCmd(1L, "Alice Smith", "alice@example.com"));
        orderService.updateOrderStatus(result.orderNumber(), OrderStatus.IN_PROCESS);
        orderService.updateOrderStatus(result.orderNumber(), OrderStatus.DELIVERED);

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", result.orderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .doesNotContain("Mark as In Process")
                .doesNotContain("Mark as Delivered")
                .doesNotContain("Cancel Order");
    }

    private static CreateOrderCmd buildCreateOrderCmd(Long userId, String customerName, String email) {
        OrderItem item = new OrderItem("P100", "The Hunger Games", new BigDecimal("34.0"), 1);
        return new CreateOrderCmd(
                new CreateOrderCmd.UserId(userId),
                new Customer(customerName, email, "9999999999"),
                "Test Address",
                item);
    }
}

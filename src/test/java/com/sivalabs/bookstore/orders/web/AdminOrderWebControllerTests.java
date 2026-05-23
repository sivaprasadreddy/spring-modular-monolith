package com.sivalabs.bookstore.orders.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.catalog.ProductApi;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderMapper;
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
        orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));
        orderService.createOrder(buildOrderEntity(2L, "Bob Jones", "bob@example.com"));
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
        OrderEntity saved = orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", saved.getOrderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Alice Smith");
    }

    @Test
    void shouldShowOrderStatusOnDetailPage() {
        OrderEntity saved = orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", saved.getOrderNumber())
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
        OrderEntity saved = orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", saved.getOrderNumber())
                        .header("HX-Request", "true")
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .doesNotContain("admin-topbar");
    }

    @Test
    void shouldLinkOrderNumberInListToDetailPage() {
        OrderEntity saved = orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester.get().uri("/admin/orders").with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("/admin/orders/" + saved.getOrderNumber());
    }

    @Test
    void shouldUpdateOrderStatusAndRedirectToDetailPage() {
        OrderEntity saved = orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .post()
                        .uri("/admin/orders/{orderNumber}/status", saved.getOrderNumber())
                        .param("status", "IN_PROCESS")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.FOUND)
                .hasHeader("Location", "/admin/orders/" + saved.getOrderNumber());
    }

    @Test
    void shouldShowMarkAsInProcessButtonForNewOrder() {
        OrderEntity saved = orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", saved.getOrderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Mark as In Process");
    }

    @Test
    void shouldShowCancelButtonForNewOrder() {
        OrderEntity saved = orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", saved.getOrderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Cancel Order");
    }

    @Test
    void shouldNotShowTransitionButtonsForDeliveredOrder() {
        OrderEntity saved = orderService.createOrder(buildOrderEntity(1L, "Alice Smith", "alice@example.com"));
        orderService.updateOrderStatus(saved.getOrderNumber(), OrderStatus.IN_PROCESS);
        orderService.updateOrderStatus(saved.getOrderNumber(), OrderStatus.DELIVERED);

        assertThat(mockMvcTester
                        .get()
                        .uri("/admin/orders/{orderNumber}", saved.getOrderNumber())
                        .with(user("admin").roles("ADMIN")))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .doesNotContain("Mark as In Process")
                .doesNotContain("Mark as Delivered")
                .doesNotContain("Cancel Order");
    }

    private static OrderEntity buildOrderEntity(Long userId, String customerName, String email) {
        OrderItem item = new OrderItem("P100", "The Hunger Games", new BigDecimal("34.0"), 1);
        CreateOrderCmd cmd = new CreateOrderCmd(
                new CreateOrderCmd.UserId(userId),
                new Customer(customerName, email, "9999999999"),
                "Test Address",
                item);
        return OrderMapper.convertToEntity(cmd);
    }
}

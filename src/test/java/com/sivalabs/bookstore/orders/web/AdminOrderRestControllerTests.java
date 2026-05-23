package com.sivalabs.bookstore.orders.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.catalog.ProductApi;
import com.sivalabs.bookstore.common.models.PagedResult;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderMapper;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderCmd;
import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ApplicationModuleTest(
        webEnvironment = RANDOM_PORT,
        extraIncludes = {"config", "users"})
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class AdminOrderRestControllerTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private OrderService orderService;

    @MockitoBean
    ProductApi productApi;

    @BeforeEach
    void setUp() {
        orderService.createOrder(buildOrderEntity(1L));
        orderService.createOrder(buildOrderEntity(2L));
    }

    @Test
    void shouldReturn200WithOrdersForAdminUser() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(paged -> {
                    PagedResult<?> pr = (PagedResult<?>) paged;
                    assertThat(pr.totalElements()).isGreaterThanOrEqualTo(2);
                    assertThat(pr.data()).isNotEmpty();
                });
    }

    @Test
    void shouldFilterOrdersByStatus() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/orders?status=NEW")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(paged -> {
                    PagedResult<?> pr = (PagedResult<?>) paged;
                    assertThat(pr.totalElements()).isGreaterThanOrEqualTo(2);
                });
    }

    @Test
    void shouldReturnEmptyListForStatusWithNoOrders() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/orders?status=DELIVERED")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PagedResult.class)
                .satisfies(paged -> {
                    PagedResult<?> pr = (PagedResult<?>) paged;
                    assertThat(pr.totalElements()).isEqualTo(0);
                });
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        assertThat(mockMvcTester.get().uri("/api/admin/orders")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403ForNonAdminUser() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/admin/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    private static OrderEntity buildOrderEntity(Long userId) {
        OrderItem item = new OrderItem("P100", "The Hunger Games", new BigDecimal("34.0"), 1);
        CreateOrderCmd cmd = new CreateOrderCmd(
                new CreateOrderCmd.UserId(userId),
                new Customer("Test User", "test@example.com", "9999999999"),
                "Test Address",
                item);
        return OrderMapper.convertToEntity(cmd);
    }
}

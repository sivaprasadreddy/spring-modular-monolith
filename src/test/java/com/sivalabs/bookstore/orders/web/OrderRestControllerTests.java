package com.sivalabs.bookstore.orders.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.catalog.ProductApi;
import com.sivalabs.bookstore.catalog.ProductDto;
import com.sivalabs.bookstore.orders.CreateOrderRequest;
import com.sivalabs.bookstore.orders.OrderDto;
import com.sivalabs.bookstore.orders.domain.OrderEntity;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderCreatedEvent;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import com.sivalabs.bookstore.orders.mappers.OrderMapper;
import com.sivalabs.bookstore.users.domain.JwtTokenHelper;
import com.sivalabs.bookstore.users.domain.UserDto;
import com.sivalabs.bookstore.users.domain.UserService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ApplicationModuleTest(
        webEnvironment = RANDOM_PORT,
        extraIncludes = {"config", "users"})
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class OrderRestControllerTests {
    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @MockitoBean
    ProductApi productApi;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        ProductDto product = new ProductDto("P100", "The Hunger Games", "", null, new BigDecimal("34.0"));
        given(productApi.getByCode("P100")).willReturn(Optional.of(product));
    }

    private String createJwtToken(String email) {
        UserDto userDto = userService.findByEmail(email).orElseThrow();
        return jwtTokenHelper.generateToken(userDto).token();
    }

    @Test
    void shouldCreateOrderSuccessfully(AssertablePublishedEvents events) {
        assertThat(
                        mockMvcTester
                                .post()
                                .uri("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createJwtToken("siva@gmail.com"))
                                .content(
                                        """
                                                {
                                                    "customer": {
                                                        "name": "Siva",
                                                        "email": "siva123@gmail.com",
                                                        "phone": "9876523456"
                                                   },
                                                    "deliveryAddress": "James, Bangalore, India",
                                                    "item":{
                                                            "code": "P100",
                                                            "name": "The Hunger Games",
                                                            "price": 34.0,
                                                            "quantity": 1
                                                    }
                                                }
                                                """))
                .hasStatus(HttpStatus.CREATED);

        assertThat(events)
                .contains(OrderCreatedEvent.class)
                .matching(e -> e.customer().email(), "siva123@gmail.com")
                .matching(OrderCreatedEvent::productCode, "P100");
    }

    @Test
    void shouldReturnNotFoundWhenOrderIdNotExist() {
        assertThat(mockMvcTester
                        .get()
                        .uri("/api/orders/{orderNumber}", "non-existing-order-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + createJwtToken("siva@gmail.com")))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetOrderSuccessfully() {
        OrderEntity orderEntity = buildOrderEntity(2L);
        OrderEntity savedOrder = orderService.createOrder(orderEntity);

        assertThat(mockMvcTester
                        .get()
                        .uri("/api/orders/{orderNumber}", savedOrder.getOrderNumber())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + createJwtToken("siva@gmail.com")))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(OrderDto.class)
                .satisfies(order -> {
                    assertThat(order.orderNumber()).isEqualTo(savedOrder.getOrderNumber());
                });
    }

    @Test
    void shouldGetOrdersSuccessfully() {
        OrderEntity orderEntity = buildOrderEntity(2L);
        orderService.createOrder(orderEntity);

        assertThat(mockMvcTester
                        .get()
                        .uri("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + createJwtToken("siva@gmail.com")))
                .hasStatus(HttpStatus.OK);
    }

    private static OrderEntity buildOrderEntity(Long userId) {
        CreateOrderRequest request = buildCreateOrderRequest(userId);
        return OrderMapper.convertToEntity(request);
    }

    private static CreateOrderRequest buildCreateOrderRequest(Long userId) {
        OrderItem item = new OrderItem("P100", "The Hunger Games", new BigDecimal("34.0"), 1);
        return new CreateOrderRequest(
                new CreateOrderRequest.UserId(userId),
                new Customer("Siva", "siva@gmail.com", "77777777"),
                "Siva, Hyderabad, India",
                item);
    }
}

package com.sivalabs.bookstore.orders.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.catalog.Product;
import com.sivalabs.bookstore.catalog.ProductService;
import com.sivalabs.bookstore.orders.OrderService;
import com.sivalabs.bookstore.orders.domain.events.OrderCreatedEvent;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderRequest;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderResponse;
import com.sivalabs.bookstore.orders.domain.models.Customer;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ApplicationModuleTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class OrderRestControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @MockitoBean
    ProductService productService;

    @BeforeEach
    void setUp() {
        Product product = new Product("P100", "The Hunger Games", "", null, new BigDecimal("34.0"));
        given(productService.getByCode("P100")).willReturn(Optional.of(product));
    }

    @Test
    void shouldCreateOrderSuccessfully(AssertablePublishedEvents events) throws Exception {
        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
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
                .andExpect(status().isCreated());

        assertThat(events)
                .contains(OrderCreatedEvent.class)
                .matching(e -> e.customer().email(), "siva123@gmail.com")
                .matching(OrderCreatedEvent::productCode, "P100");
    }

    @Test
    void shouldReturnNotFoundWhenOrderIdNotExist() throws Exception {
        mockMvc.perform(get("/api/orders/{orderNumber}", "non-existing-order-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetOrderSuccessfully() throws Exception {
        CreateOrderRequest request = buildCreateOrderRequest();
        CreateOrderResponse createOrderResponse = orderService.createOrder(request);

        mockMvc.perform(get("/api/orders/{orderNumber}", createOrderResponse.orderNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", is(createOrderResponse.orderNumber())));
    }

    @Test
    void shouldGetOrdersSuccessfully() throws Exception {
        CreateOrderRequest request = buildCreateOrderRequest();
        orderService.createOrder(request);

        mockMvc.perform(get("/api/orders")).andExpect(status().isOk());
    }

    private static CreateOrderRequest buildCreateOrderRequest() {
        OrderItem item = new OrderItem("P100", "The Hunger Games", new BigDecimal("34.0"), 1);
        return new CreateOrderRequest(
                new Customer("Siva", "siva@gmail.com", "77777777"), "Siva, Hyderabad, India", item);
    }
}

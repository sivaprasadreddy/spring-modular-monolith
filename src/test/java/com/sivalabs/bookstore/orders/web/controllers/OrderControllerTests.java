package com.sivalabs.bookstore.orders.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import com.sivalabs.bookstore.catalog.Product;
import com.sivalabs.bookstore.catalog.ProductService;
import com.sivalabs.bookstore.customers.Customer;
import com.sivalabs.bookstore.customers.CustomerService;
import com.sivalabs.bookstore.orders.domain.OrderService;
import com.sivalabs.bookstore.orders.domain.events.OrderCreatedEvent;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderRequest;
import com.sivalabs.bookstore.orders.domain.models.CreateOrderResponse;
import com.sivalabs.bookstore.orders.domain.models.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ApplicationModuleTest(webEnvironment = RANDOM_PORT, classes = TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class OrderControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    CustomerService customerService;

    @BeforeEach
    void setUp() {
        Product product = new Product("P100", "The Hunger Games", "", null, new BigDecimal("34.0"));
        BDDMockito.given(productService.getByCode("P100")).willReturn(Optional.of(product));
        Customer customer1 = new Customer(1L, "Siva", "siva@gmail.com", "77777777");
        Customer customer2 = new Customer(2L, "Prasad", "prasad@gmail.com", "888888888");
        Customer customer3 = new Customer(3L, "Ramu", "ramu@gmail.com", "9999999");
        BDDMockito.given(customerService.getById(1L)).willReturn(Optional.of(customer1));
        BDDMockito.given(customerService.getByIds(Set.of(1L, 2L, 3L)))
                .willReturn(List.of(customer1, customer2, customer3));
    }

    @Test
    void shouldCreateOrderSuccessfully(AssertablePublishedEvents events) throws Exception {
        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                    {
                        "customerId": 1,
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
                .matching(OrderCreatedEvent::customerId, 1L)
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
        return new CreateOrderRequest(1L, "Siva, Hyderabad, India", item);
    }
}

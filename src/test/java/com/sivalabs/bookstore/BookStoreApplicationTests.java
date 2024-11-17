package com.sivalabs.bookstore;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestcontainersConfiguration.class)
class BookStoreApplicationTests {

    @Test
    void contextLoads() {}
}

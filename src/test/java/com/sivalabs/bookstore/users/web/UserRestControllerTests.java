package com.sivalabs.bookstore.users.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.sivalabs.bookstore.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ApplicationModuleTest(
        webEnvironment = RANDOM_PORT,
        extraIncludes = {"config"})
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class UserRestControllerTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    @DisplayName("Given valid credentials, user should be able to login successfully")
    void shouldLoginSuccessfully() {
        String requestBody = """
           {
               "email": "siva@gmail.com",
               "password": "siva"
           }
           """;
        assertThat(mockMvcTester
                        .post()
                        .uri("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(requestBody))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(UserRestController.LoginResponse.class)
                .satisfies(response -> {
                    assertThat(response.email()).isEqualTo("siva@gmail.com");
                    assertThat(response.token()).isNotBlank();
                    assertThat(response.name()).isEqualTo("Siva");
                });
    }

    @Test
    @DisplayName("Given valid user details, user should be created successfully")
    void shouldCreateUserSuccessfully() {
        String requestBody = """
           {
                "name":"User123",
                "email":"user123@gmail.com",
                "password":"secret"
           }
           """;
        assertThat(mockMvcTester
                        .post()
                        .uri("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(requestBody))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(UserRestController.LoginResponse.class)
                .satisfies(response -> {
                    assertThat(response.email()).isEqualTo("user123@gmail.com");
                    assertThat(response.role()).isEqualTo("ROLE_USER");
                    assertThat(response.name()).isEqualTo("User123");
                });
    }
}

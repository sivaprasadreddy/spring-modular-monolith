package com.sivalabs.bookstore;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestcontainersConfiguration {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"));

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.2.2-alpine"));

    @Container
    static LgtmStackContainer lgtm = new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.17.0"));

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return postgres;
    }

    @Bean
    @ServiceConnection
    RabbitMQContainer rabbitmq() {
        return rabbitmq;
    }

    @Bean
    @ServiceConnection
    LgtmStackContainer lgtm() {
        return lgtm;
    }
}

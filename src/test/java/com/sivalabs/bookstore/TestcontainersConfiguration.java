package com.sivalabs.bookstore;

import static org.testcontainers.utility.DockerImageName.parse;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestcontainersConfiguration {

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer(parse("rabbitmq:4.3.6-alpine"));

    @Container
    static LgtmStackContainer lgtm = new LgtmStackContainer(parse("grafana/otel-lgtm:0.36.0"));

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer(parse("postgres:18-alpine"));
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

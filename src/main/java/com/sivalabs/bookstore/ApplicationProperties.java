package com.sivalabs.bookstore;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record ApplicationProperties(
        String supportEmail,
        String newsletterJobCron,
        @DefaultValue("10") int postsPerPage,
        JwtProperties jwt,
        CorsProperties cors,
        OpenAPIProperties openApi) {
    public record JwtProperties(
            @DefaultValue("SivaLabs") String issuer,
            @DefaultValue("604800") Long expiresInSeconds,
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey) {}

    public record CorsProperties(
            @DefaultValue("/api/**") String pathPattern,
            @DefaultValue("*") String allowedOrigins,
            @DefaultValue("*") String allowedMethods,
            @DefaultValue("*") String allowedHeaders) {}

    public record OpenAPIProperties(
            @DefaultValue("BookStore API") String title,

            @DefaultValue("BookStore API Swagger Documentation")
            String description,

            @DefaultValue("v1.0.0") String version,
            Contact contact) {

        public record Contact(
                @DefaultValue("SivaLabs") String name,
                @DefaultValue("support@sivalabs.in") String email) {}
    }
}

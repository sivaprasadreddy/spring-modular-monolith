package com.sivalabs.bookstore.users.domain;

import com.sivalabs.bookstore.ApplicationProperties;
import java.time.Instant;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenHelper {
    private final JwtEncoder encoder;
    private final ApplicationProperties properties;

    JwtTokenHelper(JwtEncoder encoder, ApplicationProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public JwtToken generateToken(UserDto userDto) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.jwt().expiresInSeconds());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(userDto.email())
                .claim("user_id", userDto.id())
                .claim("roles", userDto.role().name())
                .build();
        var token = this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new JwtToken(token, expiresAt);
    }
}

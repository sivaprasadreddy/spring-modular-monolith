package com.sivalabs.bookstore.users.domain.models;

import java.time.Instant;

public record JwtToken(String token, Instant expiresAt) {}

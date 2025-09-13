package com.sivalabs.bookstore.users.domain;

import java.time.Instant;

public record JwtToken(String token, Instant expiresAt) {}

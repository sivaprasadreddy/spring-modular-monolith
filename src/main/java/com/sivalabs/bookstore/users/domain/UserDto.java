package com.sivalabs.bookstore.users.domain;

public record UserDto(Long id, String name, String email, String password, Role role) {}

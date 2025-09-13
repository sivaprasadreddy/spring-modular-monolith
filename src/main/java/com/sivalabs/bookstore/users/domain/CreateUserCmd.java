package com.sivalabs.bookstore.users.domain;

public record CreateUserCmd(String name, String email, String password, Role role) {}

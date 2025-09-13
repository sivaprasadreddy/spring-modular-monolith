package com.sivalabs.bookstore.users.domain;

public enum Role {
    ROLE_USER,
    ROLE_AUTHOR,
    ROLE_ADMIN;

    public static String getRoleHierarchy() {
        return ROLE_ADMIN.name() + " > " + ROLE_AUTHOR.name() + " > " + ROLE_USER.name();
    }
}

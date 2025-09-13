package com.sivalabs.bookstore.users.domain;

public class UserMapper {
    public static UserDto toUser(UserEntity entity) {
        return new UserDto(entity.getId(), entity.getName(), entity.getEmail(), entity.getPassword(), entity.getRole());
    }
}

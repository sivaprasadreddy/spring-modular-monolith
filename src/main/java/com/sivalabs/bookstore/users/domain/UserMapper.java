package com.sivalabs.bookstore.users.domain;

import com.sivalabs.bookstore.users.domain.models.UserDto;

class UserMapper {
    public static UserDto toUser(UserEntity entity) {
        return new UserDto(entity.getId(), entity.getName(), entity.getEmail(), entity.getPassword(), entity.getRole());
    }
}

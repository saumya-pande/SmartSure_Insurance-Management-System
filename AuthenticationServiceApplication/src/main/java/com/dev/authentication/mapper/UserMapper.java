package com.dev.authentication.mapper;

import org.mapstruct.Mapper;

import com.dev.authentication.dto.UserResponse;
import com.dev.authentication.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);
}
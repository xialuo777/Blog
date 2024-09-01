package com.blog.mapper;

import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void insertUser(User user);
    void updateUser(User user);
    void deleteByEmail(String Email);
    User findByEmail(String Email);

}

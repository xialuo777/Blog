package com.blog.mapper;

import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    int insertUser(User user);
    int deleteByEmail(String Email);
    User findByEmail(String Email);
    int deleteByPrimaryKey(Long userId);
    int insertSelective(User user);
    User selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
}

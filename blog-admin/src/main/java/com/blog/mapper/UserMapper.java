package com.blog.mapper;

import com.blog.entity.User;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int insertUser(User user);

    int deleteByEmail(String Email);

    User findByEmail(String Email);

    List<User> selectUsersByNickName(String nickName);

    List<User> selectUsers();

    int selectTotalCount();

    User selectUserByComment(Long userId);
}
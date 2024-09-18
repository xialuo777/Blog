package com.blog.mapper;

import com.blog.entity.User;

import java.util.List;

/**
 * @author: zhang
 * @time: 2024-09-14 11:24
 */
public interface UserMapper {
    /**
     * 根据主键删除
     * @param userId 主键
     * @return int
     */
    int deleteByPrimaryKey(Long userId);

    /**
     * 插入用户信息
     * @param record 用户信息
     * @return int
     */
    int insert(User record);

    /**
     * 选择性插入用户信息
     * @param record 用户信息
     * @return int
     */
    int insertSelective(User record);

    /**
     * 根据主键查询用户信息
     * @param userId 主键
     * @return User
     */
    User selectByPrimaryKey(Long userId);

    /**
     * 根据主键选择性地更新用户信息
     * @param record 用户信息
     * @return int
     */
    int updateByPrimaryKeySelective(User record);

    /**
     * 根据主键更新用户信息
     * @param record 用户信息
     * @return int
     */
    int updateByPrimaryKey(User record);

    /**
     * 插入用户信息
     * @param user 用户信息
     * @return int
     */
    int insertUser(User user);

    /**
     * 根据邮箱删除用户信息
     * @param email 邮箱
     * @return int
     */
    int deleteByEmail(String email);

    /**
     * 根据邮箱查询用户信息
     * @param email 邮箱
     * @return User
     */
    User findByEmail(String email);

    /**
     * 根据昵称查询用户信息
     * @param nickName 昵称
     * @return List<User>
     */
    List<User> selectUsersByNickName(String nickName);

    /**
     * 查询所有用户信息
     * @return List<User>
     */
    List<User> selectUsers();

    /**
     * 查询用户总数
     * @return int
     */
    int selectTotalCount();

}
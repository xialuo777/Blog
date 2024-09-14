package com.blog.mapper;

import com.blog.entity.Admin;

/**
 * @author 24360
 */
public interface AdminMapper {
    /**
     * 根据主键删除
     * @param adminId 管理员id
     * @return int
     */
    int deleteByPrimaryKey(Long adminId);

    /**
     * 插入管理员信息
     * @param record 管理员信息
     * @return int
     */

    int insert(Admin record);

    /**
     * 插入管理员信息
     * @param record 管理员信息
     * @return int
     */

    int insertSelective(Admin record);

    /**
     * 根据主键查询管理员信息
     * @param adminId 管理员id
     * @return Admin
     */
    Admin selectByPrimaryKey(Long adminId);

    /**
     * 根据主键更新管理员信息
     * @param record 管理员信息
     * @return Admin
     */
    int updateByPrimaryKeySelective(Admin record);

    /**
     * 根据主键更新管理员信息
     * @param record 管理员信息
     * @return Admin
     */
    int updateByPrimaryKey(Admin record);

    /**
     * 根据账号查询管理员信息
     * @param account 管理员账号
     * @return Admin
     */
    Admin selectByAccount(String account);
}
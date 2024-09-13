package com.blog.mapper;

import com.blog.entity.Admin;

/**
 * @author 24360
 */
public interface AdminMapper {
    int deleteByPrimaryKey(Long adminId);

    int insert(Admin record);

    int insertSelective(Admin record);

    Admin selectByPrimaryKey(Long adminId);

    int updateByPrimaryKeySelective(Admin record);

    int updateByPrimaryKey(Admin record);

    Admin selectByAccount(String account);
}
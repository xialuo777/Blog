package com.blog.service;

import com.blog.entity.Admin;
import com.blog.exception.BusinessException;
import com.blog.mapper.AdminMapper;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.admin.AdminInVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 管理员模块业务层
 *
 * @author : [24360]
 * @version : [v1.0]
 * @time : [2024/9/13 15:41]
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final AdminMapper adminMapper;
    private final JwtProcessor jwtProcessor;
    private final RedisProcessor redisProcessor;

    public LoginResponse adminLogin(AdminInVo adminInVo) {
        Admin admin = Optional.ofNullable(adminMapper.selectByAccount(adminInVo.getAccount()))
                .orElseThrow(() -> new BusinessException("该管理员账号不存在"));
        if (!admin.getPassword().equals(adminInVo.getPassword())) {
            throw new BusinessException("密码错误");
        }
        Map<String, Object> adminMap = new HashMap<>(2);
        adminMap.put("account",admin.getAccount());
        adminMap.put("id",admin.getAdminId());
        String accessToken = jwtProcessor.generateToken(adminMap);
        String refreshToken = jwtProcessor.generateRefreshToken(adminMap);

        redisProcessor.set(RedisTransKey.tokenKey(admin.getAccount()),accessToken,7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.refreshTokenKey(admin.getAccount()),refreshToken,7, TimeUnit.DAYS);

        return new LoginResponse(accessToken, refreshToken);
    }
    public Optional<Admin> getAdminById(Long adminId) {
        return Optional.ofNullable(adminMapper.selectByPrimaryKey(adminId));
    }

    public void updateAdmin(Admin admin) {
        adminMapper.updateByPrimaryKeySelective(admin);
    }
}

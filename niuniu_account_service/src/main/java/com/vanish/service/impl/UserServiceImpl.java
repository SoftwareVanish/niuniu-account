package com.vanish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vanish.common.exception.BusinessException;
import com.vanish.common.util.IdGenerator;
import com.vanish.common.util.JwtUtil;
import com.vanish.dao.entity.User;
import com.vanish.dao.mapper.UserMapper;
import com.vanish.service.IUserService;
import com.vanish.service.dto.UserLoginDTO;
import com.vanish.service.dto.UserUpdateDTO;
import com.vanish.service.vo.LoginVO;
import com.vanish.service.vo.UserInfoVO;
import com.vanish.service.vo.UserUpdateVO;
import com.vanish.service.wechat.WeChatApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final WeChatApiClient weChatApiClient;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(UserLoginDTO dto) {
        // code 换 openid
        String openid = weChatApiClient.code2Session(dto.getCode());

        // 按 openid 查用户，不存在则注册
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid)
                .last("LIMIT 1"));
        if (user == null) {
            user = new User();
            user.setUserId(IdGenerator.next("u"));
            user.setOpenid(openid);
            user.setNickName(dto.getNickName());
            user.setAvatarUrl(dto.getAvatarUrl());
            user.setLoginTime(System.currentTimeMillis());
            user.setCreateBy(user.getUserId());
            userMapper.insert(user);
            log.info("UserServiceImpl.login | register | userId:{}", user.getUserId());
        } else {
            // 已有用户：更新昵称、头像和登录时间
            User update = new User();
            update.setUserId(user.getUserId());
            update.setNickName(dto.getNickName());
            update.setAvatarUrl(dto.getAvatarUrl());
            update.setLoginTime(System.currentTimeMillis());
            update.setUpdateBy(user.getUserId());
            userMapper.updateById(update);
            user.setNickName(dto.getNickName());
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        return LoginVO.builder()
                .token(jwtUtil.generateToken(user.getUserId()))
                .userId(user.getUserId())
                .nickName(user.getNickName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public UserInfoVO getUserInfo(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserInfoVO.builder()
                .userId(user.getUserId())
                .nickName(user.getNickName())
                .avatarUrl(user.getAvatarUrl())
                .loginTime(user.getLoginTime())
                .build();
    }

    @Override
    public UserUpdateVO updateUserInfo(String userId, UserUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        User update = new User();
        update.setUserId(userId);
        if (dto.getNickName() != null) {
            update.setNickName(dto.getNickName());
        }
        if (dto.getAvatarUrl() != null) {
            update.setAvatarUrl(dto.getAvatarUrl());
        }
        update.setUpdateBy(userId);
        userMapper.updateById(update);

        String nickName = Objects.requireNonNullElse(dto.getNickName(), user.getNickName());
        String avatarUrl = Objects.requireNonNullElse(dto.getAvatarUrl(), user.getAvatarUrl());
        return UserUpdateVO.builder()
                .nickName(nickName)
                .avatarUrl(avatarUrl)
                .build();
    }
}

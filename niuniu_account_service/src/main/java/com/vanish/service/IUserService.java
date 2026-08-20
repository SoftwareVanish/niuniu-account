package com.vanish.service;

import com.vanish.service.dto.UserLoginDTO;
import com.vanish.service.dto.UserUpdateDTO;
import com.vanish.service.vo.LoginVO;
import com.vanish.service.vo.UserInfoVO;
import com.vanish.service.vo.UserUpdateVO;

/**
 * 用户服务接口
 */
public interface IUserService {

    /**
     * 微信登录：code 换 openid，用户不存在则注册，签发 JWT
     *
     * @param dto 登录参数（code + 昵称 + 头像）
     * @return 登录结果（token + 用户信息）
     */
    LoginVO login(UserLoginDTO dto);

    /**
     * 获取用户信息
     *
     * @param userId 当前登录用户 ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(String userId);

    /**
     * 更新用户信息（仅更新传入字段）
     *
     * @param userId 当前登录用户 ID
     * @param dto    更新参数（昵称 / 头像）
     * @return 更新后的昵称和头像
     */
    UserUpdateVO updateUserInfo(String userId, UserUpdateDTO dto);
}

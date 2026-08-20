package com.vanish.controller;

import com.vanish.common.result.ResultVO;
import com.vanish.service.IUserService;
import com.vanish.service.dto.UserLoginDTO;
import com.vanish.service.dto.UserUpdateDTO;
import com.vanish.service.vo.LoginVO;
import com.vanish.service.vo.UserInfoVO;
import com.vanish.service.vo.UserUpdateVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模块接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 1.1 微信登录
     */
    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return ResultVO.successWithData(userService.login(dto));
    }

    /**
     * 1.2 获取用户信息
     */
    @GetMapping("/info")
    public ResultVO<UserInfoVO> info(@RequestAttribute("userId") String userId) {
        return ResultVO.successWithData(userService.getUserInfo(userId));
    }

    /**
     * 1.3 更新用户信息
     */
    @PutMapping("/info")
    public ResultVO<UserUpdateVO> updateInfo(@RequestAttribute("userId") String userId,
                                             @Valid @RequestBody UserUpdateDTO dto) {
        return ResultVO.successWithData(userService.updateUserInfo(userId, dto));
    }

    /**
     * 1.4 退出登录（JWT 无状态，服务端无需处理）
     */
    @PostMapping("/logout")
    public ResultVO<Void> logout() {
        return ResultVO.successWithMessage("已退出登录");
    }
}

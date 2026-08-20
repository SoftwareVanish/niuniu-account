package com.vanish.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 微信登录请求 DTO
 */
@Data
public class UserLoginDTO {

    /** wx.login() 获取的临时登录凭证 */
    @NotBlank(message = "code 不能为空")
    private String code;

    /** 用户昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    private String nickName;

    /** 头像路径或网络 URL */
    @Size(max = 512, message = "头像 URL 长度不能超过 512 个字符")
    private String avatarUrl;
}

package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应 VO
 */
@Data
@Builder
public class LoginVO {

    /** JWT token */
    private String token;

    /** 用户 ID */
    private String userId;

    /** 用户昵称 */
    private String nickName;

    /** 头像 URL */
    private String avatarUrl;
}

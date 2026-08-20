package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 更新用户信息响应 VO
 */
@Data
@Builder
public class UserUpdateVO {

    /** 用户昵称 */
    private String nickName;

    /** 头像 URL */
    private String avatarUrl;
}

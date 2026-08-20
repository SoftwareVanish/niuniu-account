package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息 VO
 */
@Data
@Builder
public class UserInfoVO {

    /** 用户 ID */
    private String userId;

    /** 用户昵称 */
    private String nickName;

    /** 头像 URL */
    private String avatarUrl;

    /** 最近登录时间戳（毫秒） */
    private Long loginTime;
}

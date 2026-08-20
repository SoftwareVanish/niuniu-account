package com.vanish.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vanish.common.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends CommonEntity {

    /** 用户ID（u_开头） */
    @TableId(type = IdType.INPUT)
    private String userId;

    /** 微信openid */
    private String openid;

    /** 用户昵称 */
    private String nickName;

    /** 头像URL */
    private String avatarUrl;

    /** 最近登录时间戳（毫秒） */
    private Long loginTime;
}

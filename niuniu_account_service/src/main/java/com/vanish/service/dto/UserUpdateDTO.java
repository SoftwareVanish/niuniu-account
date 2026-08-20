package com.vanish.service.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求 DTO
 */
@Data
public class UserUpdateDTO {

    /** 新昵称（不传则不修改） */
    @Size(max = 64, message = "昵称长度不能超过 64 个字符")
    private String nickName;

    /** 新头像 URL（不传则不修改） */
    @Size(max = 512, message = "头像 URL 长度不能超过 512 个字符")
    private String avatarUrl;
}

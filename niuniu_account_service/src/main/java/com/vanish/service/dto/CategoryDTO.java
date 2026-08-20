package com.vanish.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增自定义分类请求 DTO
 */
@Data
public class CategoryDTO {

    /** 分类类型：expense 支出 / income 收入 */
    @NotBlank(message = "分类类型不能为空")
    @Pattern(regexp = "expense|income", message = "分类类型只能是 expense 或 income")
    private String type;

    /** 分类名称 */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称长度不能超过 32 个字符")
    private String name;

    /** 图标标识 */
    @NotBlank(message = "图标不能为空")
    @Size(max = 32, message = "图标长度不能超过 32 个字符")
    private String icon;
}

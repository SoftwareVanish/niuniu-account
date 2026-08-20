package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 分类列表 VO（预设 + 用户自定义）
 */
@Data
@Builder
public class CategoryListVO {

    /** 系统预设分类 */
    private List<CategoryVO> preset;

    /** 用户自定义分类 */
    private List<CategoryVO> custom;
}

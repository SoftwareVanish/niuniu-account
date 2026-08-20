package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 分类 VO（预设分类 id 为 null）
 */
@Data
@Builder
public class CategoryVO {

    /** 分类 ID（预设分类为 null） */
    private String id;

    /** 分类名称 */
    private String name;

    /** 图标标识 */
    private String icon;
}

package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 分类占比统计 VO
 */
@Data
@Builder
public class CategoryStatVO {

    /** 分组 key：{type}_{category} */
    private String key;

    /** 账单类型：expense 支出 / income 收入 */
    private String type;

    /** 分类名称 */
    private String category;

    /** 分类图标标识 */
    private String categoryIcon;

    /** 金额合计（分） */
    private Long amount;

    /** 占比（%，保留 1 位小数） */
    private Double percentage;
}

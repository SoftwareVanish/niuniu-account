package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 账单 VO
 */
@Data
@Builder
public class BillVO {

    /** 账单 ID */
    private String id;

    /** 账单类型：expense 支出 / income 收入 */
    private String type;

    /** 金额（分） */
    private Long amount;

    /** 分类名称 */
    private String category;

    /** 分类图标标识 */
    private String categoryIcon;

    /** 记账日期 YYYY-MM-DD */
    private String date;

    /** 备注 */
    private String note;

    /** 创建时间戳（毫秒） */
    private Long createTime;
}

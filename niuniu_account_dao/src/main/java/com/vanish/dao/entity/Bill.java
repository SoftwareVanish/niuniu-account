package com.vanish.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vanish.common.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_bill")
public class Bill extends CommonEntity {

    /** 账单ID（b_开头） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 归属用户ID */
    private String userId;

    /** 账单类型（expense=支出 income=收入） */
    private String type;

    /** 金额（分） */
    private Long amount;

    /** 分类名称 */
    private String category;

    /** 分类图标标识 */
    private String categoryIcon;

    /** 记账日期（YYYY-MM-DD） */
    private String date;

    /** 备注 */
    private String note;
}

package com.vanish.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vanish.common.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义分类实体（预设分类不落库）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_custom_category")
public class CustomCategory extends CommonEntity {

    /** 分类ID（c_开头） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 归属用户ID */
    private String userId;

    /** 分类类型（expense=支出 income=收入） */
    private String type;

    /** 分类名称 */
    private String name;

    /** 图标标识 */
    private String icon;
}

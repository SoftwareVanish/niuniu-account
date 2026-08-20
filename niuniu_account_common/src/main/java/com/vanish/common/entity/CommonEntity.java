package com.vanish.common.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

/**
 * 实体通用基类：审计字段 + 逻辑删除
 * createTime / updateTime 由数据库默认值维护，业务代码不手动设置
 */
@Data
public class CommonEntity {

    /** 创建人 */
    private String createBy;

    /** 创建时间（数据库默认 CURRENT_TIMESTAMP） */
    private Date createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间（数据库默认 CURRENT_TIMESTAMP ON UPDATE） */
    private Date updateTime;

    /** 状态（1=正常 0=删除），逻辑删除标记 */
    @TableLogic
    private Integer status;
}

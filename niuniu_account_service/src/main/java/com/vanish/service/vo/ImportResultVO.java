package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 账单导入结果 VO
 */
@Data
@Builder
public class ImportResultVO {

    /** 成功导入条数 */
    private Integer importedCount;

    /** 跳过条数（含重复和字段不完整） */
    private Integer skippedCount;
}

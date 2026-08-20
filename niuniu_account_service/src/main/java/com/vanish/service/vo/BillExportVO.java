package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 账单导出 VO（格式对齐前端导出：{ app, version, exportTime, bills }）
 */
@Data
@Builder
public class BillExportVO {

    /** 应用标识 */
    private String app;

    /** 版本号 */
    private String version;

    /** 导出时间戳（毫秒） */
    private Long exportTime;

    /** 账单列表 */
    private List<BillVO> bills;
}

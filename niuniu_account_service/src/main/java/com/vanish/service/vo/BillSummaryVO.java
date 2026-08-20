package com.vanish.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 账单汇总 VO（月度汇总 / 范围汇总通用）
 */
@Data
@Builder
public class BillSummaryVO {

    /** 总支出（分） */
    private Long totalExpense;

    /** 总收入（分） */
    private Long totalIncome;

    /** 结余 = 总收入 - 总支出（分） */
    private Long balance;
}

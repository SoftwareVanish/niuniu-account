package com.vanish.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改账单请求 DTO（所有字段可选，仅更新传入的字段）
 */
@Data
public class BillUpdateDTO {

    /** 账单类型：expense 支出 / income 收入 */
    @Pattern(regexp = "expense|income", message = "账单类型只能是 expense 或 income")
    private String type;

    /** 金额（分） */
    @Min(value = 1, message = "金额必须大于 0")
    @Max(value = 999999999999L, message = "金额超出上限")
    private Long amount;

    /** 分类名称 */
    @Size(max = 32, message = "分类名称长度不能超过 32 个字符")
    private String category;

    /** 分类图标标识 */
    @Size(max = 32, message = "分类图标长度不能超过 32 个字符")
    private String categoryIcon;

    /** 记账日期 YYYY-MM-DD */
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式必须为 YYYY-MM-DD")
    private String date;

    /** 备注（传空字符串可清空备注） */
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String note;
}

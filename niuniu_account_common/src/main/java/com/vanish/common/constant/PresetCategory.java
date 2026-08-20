package com.vanish.common.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 系统预设分类常量（不落库，对应前端 utils/category.js 的默认分类）
 * 支出 8 项 / 收入 5 项
 */
@Getter
public enum PresetCategory {

    // ---- 支出 ----
    FOOD("expense", "餐饮", "food"),
    TRANSPORT("expense", "交通", "transport"),
    SHOPPING("expense", "购物", "shopping"),
    ENTERTAINMENT("expense", "娱乐", "entertainment"),
    HOUSING("expense", "住房", "housing"),
    MEDICAL("expense", "医疗", "medical"),
    EDUCATION("expense", "教育", "education"),
    OTHER_EXPENSE("expense", "其他", "other"),

    // ---- 收入 ----
    SALARY("income", "工资", "salary"),
    PART_TIME("income", "兼职", "partTime"),
    INVESTMENT("income", "理财", "investment"),
    RED_ENVELOPE("income", "红包", "redEnvelope"),
    OTHER_INCOME("income", "其他", "other");

    /** 分类类型：expense 支出 / income 收入 */
    private final String type;

    /** 分类名称 */
    private final String name;

    /** 图标标识 */
    private final String icon;

    PresetCategory(String type, String name, String icon) {
        this.type = type;
        this.name = name;
        this.icon = icon;
    }

    /**
     * 按类型获取预设分类列表
     */
    public static List<PresetCategory> listByType(String type) {
        return Arrays.stream(values())
                .filter(c -> c.type.equals(type))
                .toList();
    }

    /**
     * 判断指定类型下是否存在同名预设分类
     */
    public static boolean existsName(String type, String name) {
        return listByType(type).stream().anyMatch(c -> c.name.equals(name));
    }
}

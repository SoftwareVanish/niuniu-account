package com.vanish.service;

import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.CategoryStatVO;

import java.util.List;

/**
 * 统计服务接口
 */
public interface IStatisticsService {

    /**
     * 日期范围汇总统计（总支出 / 总收入 / 结余）
     *
     * @param userId    当前登录用户 ID
     * @param startDate 开始日期 YYYY-MM-DD
     * @param endDate   结束日期 YYYY-MM-DD
     * @param type      筛选类型：all 全部（默认）/ expense 仅支出 / income 仅收入
     * @return 汇总结果
     */
    BillSummaryVO summary(String userId, String startDate, String endDate, String type);

    /**
     * 分类占比统计（按 type+category 分组，金额倒序）
     *
     * @param userId    当前登录用户 ID
     * @param startDate 开始日期 YYYY-MM-DD
     * @param endDate   结束日期 YYYY-MM-DD
     * @param type      筛选类型：all / expense / income
     * @return 分类占比列表
     */
    List<CategoryStatVO> categoryStats(String userId, String startDate, String endDate, String type);
}

package com.vanish.controller;

import com.vanish.common.result.ResultVO;
import com.vanish.service.IStatisticsService;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.CategoryStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计模块接口
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final IStatisticsService statisticsService;

    /**
     * 4.1 日期范围汇总统计
     */
    @GetMapping("/summary")
    public ResultVO<BillSummaryVO> summary(@RequestAttribute("userId") String userId,
                                           @RequestParam("startDate") String startDate,
                                           @RequestParam("endDate") String endDate,
                                           @RequestParam(value = "type", required = false) String type) {
        return ResultVO.successWithData(statisticsService.summary(userId, startDate, endDate, type));
    }

    /**
     * 4.2 分类占比统计
     */
    @GetMapping("/category")
    public ResultVO<List<CategoryStatVO>> category(@RequestAttribute("userId") String userId,
                                                   @RequestParam("startDate") String startDate,
                                                   @RequestParam("endDate") String endDate,
                                                   @RequestParam(value = "type", required = false) String type) {
        return ResultVO.successWithData(statisticsService.categoryStats(userId, startDate, endDate, type));
    }
}

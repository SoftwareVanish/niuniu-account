package com.vanish.controller;

import com.vanish.NiuniuAccountApplication;
import com.vanish.common.util.JwtUtil;
import com.vanish.service.IStatisticsService;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.CategoryStatVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 统计模块接口测试
 */
@SpringBootTest(classes = NiuniuAccountApplication.class)
@AutoConfigureMockMvc
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private IStatisticsService statisticsService;

    private String authHeader;

    @BeforeEach
    void setUp() {
        authHeader = "Bearer " + jwtUtil.generateToken("u_test");
    }

    @Test
    @DisplayName("4.1 GET /api/statistics/summary：范围汇总（type 可省略）")
    void summary() throws Exception {
        when(statisticsService.summary(anyString(), eq("2026-08-01"), eq("2026-08-31"), isNull()))
                .thenReturn(BillSummaryVO.builder()
                        .totalExpense(15000L).totalIncome(50000L).balance(35000L).build());

        mockMvc.perform(get("/api/statistics/summary")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalExpense").value(15000))
                .andExpect(jsonPath("$.data.totalIncome").value(50000));
    }

    @Test
    @DisplayName("4.1 GET /api/statistics/summary：带 type 筛选")
    void summary_withType() throws Exception {
        when(statisticsService.summary(anyString(), eq("2026-08-01"), eq("2026-08-31"), eq("expense")))
                .thenReturn(BillSummaryVO.builder()
                        .totalExpense(15000L).totalIncome(0L).balance(-15000L).build());

        mockMvc.perform(get("/api/statistics/summary")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .param("type", "expense")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.balance").value(-15000));
    }

    @Test
    @DisplayName("4.2 GET /api/statistics/category：分类占比")
    void category() throws Exception {
        when(statisticsService.categoryStats(anyString(), eq("2026-08-01"), eq("2026-08-31"), eq("all")))
                .thenReturn(List.of(
                        CategoryStatVO.builder()
                                .key("expense_餐饮").type("expense").category("餐饮")
                                .categoryIcon("food").amount(5000L).percentage(33.3).build(),
                        CategoryStatVO.builder()
                                .key("expense_交通").type("expense").category("交通")
                                .categoryIcon("transport").amount(3000L).percentage(20.0).build()));

        mockMvc.perform(get("/api/statistics/category")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .param("type", "all")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].key").value("expense_餐饮"))
                .andExpect(jsonPath("$.data[0].percentage").value(33.3))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("4.x 未带 token 访问统计接口返回 401")
    void unauthorized() throws Exception {
        mockMvc.perform(get("/api/statistics/summary")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}

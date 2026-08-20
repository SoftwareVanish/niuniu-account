package com.vanish.service;

import com.vanish.common.exception.BusinessException;
import com.vanish.service.dto.BillDTO;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.CategoryStatVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 统计服务单元测试（真实 MySQL 测试库）
 */
@SpringBootTest(classes = ServiceTestApplication.class)
class StatisticsServiceTest {

    private static final String USER_A = "u_test_a";

    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private IBillService billService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.update("DELETE FROM t_bill");
        jdbcTemplate.update("DELETE FROM t_custom_category");
        jdbcTemplate.update("DELETE FROM t_user");
    }

    private void buildDTO(String type, long amount, String category, String icon, String date) {
        BillDTO dto = new BillDTO();
        dto.setType(type);
        dto.setAmount(amount);
        dto.setCategory(category);
        dto.setCategoryIcon(icon);
        dto.setDate(date);
        billService.createBill(USER_A, dto);
    }

    /**
     * 造数：8 月支出 餐饮5000+2000、交通3000；收入 工资10000；7 月支出 购物1000（范围外）
     */
    private void initData() {
        buildDTO("expense", 5000L, "餐饮", "food", "2026-08-01");
        buildDTO("expense", 2000L, "餐饮", "food", "2026-08-02");
        buildDTO("expense", 3000L, "交通", "transport", "2026-08-03");
        buildDTO("income", 10000L, "工资", "salary", "2026-08-04");
        buildDTO("expense", 1000L, "购物", "shopping", "2026-07-31");
    }

    @Test
    @DisplayName("4.1 范围汇总：all 全部统计")
    void summary_all() {
        initData();

        BillSummaryVO vo = statisticsService.summary(USER_A, "2026-08-01", "2026-08-31", "all");

        assertEquals(10000L, vo.getTotalExpense());
        assertEquals(10000L, vo.getTotalIncome());
        assertEquals(0L, vo.getBalance());
    }

    @Test
    @DisplayName("4.1 范围汇总：expense 仅统计支出")
    void summary_expense() {
        initData();

        BillSummaryVO vo = statisticsService.summary(USER_A, "2026-08-01", "2026-08-31", "expense");

        assertEquals(10000L, vo.getTotalExpense());
        assertEquals(0L, vo.getTotalIncome(), "筛选 expense 时收入应为 0");
        assertEquals(-10000L, vo.getBalance());
    }

    @Test
    @DisplayName("4.1 范围汇总：income 仅统计收入，type 缺省按 all")
    void summary_income() {
        initData();

        BillSummaryVO income = statisticsService.summary(USER_A, "2026-08-01", "2026-08-31", "income");
        assertEquals(0L, income.getTotalExpense());
        assertEquals(10000L, income.getTotalIncome());

        // type 为空按 all 处理
        BillSummaryVO all = statisticsService.summary(USER_A, "2026-08-01", "2026-08-31", null);
        assertEquals(10000L, all.getTotalExpense());
        assertEquals(10000L, all.getTotalIncome());
    }

    @Test
    @DisplayName("4.1 范围汇总：参数校验")
    void summary_invalidParams() {
        initData();
        assertThrows(BusinessException.class,
                () -> statisticsService.summary(USER_A, "20260801", "2026-08-31", "all"), "日期格式非法");
        assertThrows(BusinessException.class,
                () -> statisticsService.summary(USER_A, "2026-08-31", "2026-08-01", "all"), "开始日期晚于结束日期");
        assertThrows(BusinessException.class,
                () -> statisticsService.summary(USER_A, "2026-08-01", "2026-08-31", "other"), "类型非法");
    }

    @Test
    @DisplayName("4.2 分类占比：all 分母为总支出+总收入，按金额倒序")
    void categoryStats_all() {
        initData();

        List<CategoryStatVO> list = statisticsService.categoryStats(USER_A, "2026-08-01", "2026-08-31", "all");

        assertEquals(3, list.size(), "应按 type+category 分为 3 组");
        // 按金额倒序：工资10000 > 餐饮7000 > 交通3000
        assertEquals("income_工资", list.get(0).getKey());
        assertEquals("expense_餐饮", list.get(1).getKey());
        assertEquals("expense_交通", list.get(2).getKey());
        // 占比：分母 20000
        assertEquals(50.0, list.get(0).getPercentage());
        assertEquals(35.0, list.get(1).getPercentage());
        assertEquals(15.0, list.get(2).getPercentage());
        assertEquals(7000L, list.get(1).getAmount());
        assertEquals("food", list.get(1).getCategoryIcon());
    }

    @Test
    @DisplayName("4.2 分类占比：expense 分母为总支出")
    void categoryStats_expense() {
        initData();

        List<CategoryStatVO> list = statisticsService.categoryStats(USER_A, "2026-08-01", "2026-08-31", "expense");

        assertEquals(2, list.size(), "仅支出分类");
        assertEquals("expense_餐饮", list.get(0).getKey());
        assertEquals(70.0, list.get(0).getPercentage());
        assertEquals(30.0, list.get(1).getPercentage());
    }

    @Test
    @DisplayName("4.2 分类占比：范围内无数据时占比为 0 且不报错")
    void categoryStats_empty() {
        initData();

        List<CategoryStatVO> list = statisticsService.categoryStats(USER_A, "2026-01-01", "2026-01-31", "all");

        assertEquals(0, list.size(), "范围内无账单应为空列表");
    }
}

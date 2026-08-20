package com.vanish.service;

import com.vanish.common.exception.BusinessException;
import com.vanish.dao.mapper.BillMapper;
import com.vanish.service.dto.BillDTO;
import com.vanish.service.dto.BillImportItemDTO;
import com.vanish.service.dto.BillUpdateDTO;
import com.vanish.service.vo.BillExportVO;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.BillVO;
import com.vanish.service.vo.ImportResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 账单服务单元测试（真实 MySQL 测试库）
 */
@SpringBootTest(classes = ServiceTestApplication.class)
class BillServiceTest {

    private static final String USER_A = "u_test_a";
    private static final String USER_B = "u_test_b";

    @Autowired
    private IBillService billService;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.update("DELETE FROM t_bill");
        jdbcTemplate.update("DELETE FROM t_custom_category");
        jdbcTemplate.update("DELETE FROM t_user");
    }

    private BillDTO buildDTO(String type, long amount, String category, String icon, String date, String note) {
        BillDTO dto = new BillDTO();
        dto.setType(type);
        dto.setAmount(amount);
        dto.setCategory(category);
        dto.setCategoryIcon(icon);
        dto.setDate(date);
        dto.setNote(note);
        return dto;
    }

    @Test
    @DisplayName("2.1 新建账单")
    void createBill() {
        BillVO vo = billService.createBill(USER_A,
                buildDTO("expense", 9999L, "餐饮", "food", "2026-08-20", "午饭"));

        assertTrue(vo.getId().startsWith("b_"), "账单 ID 应以 b_ 开头");
        assertEquals("expense", vo.getType());
        assertEquals(9999L, vo.getAmount());
        assertEquals("餐饮", vo.getCategory());
        assertEquals("food", vo.getCategoryIcon());
        assertEquals("2026-08-20", vo.getDate());
        assertEquals("午饭", vo.getNote());
        assertNotNull(vo.getCreateTime(), "创建时间戳不能为空");
        assertEquals(1L, billMapper.selectCount(null), "数据库应有 1 条账单");
    }

    @Test
    @DisplayName("2.2 修改账单：只更新传入字段")
    void updateBill_partial() {
        BillVO created = billService.createBill(USER_A,
                buildDTO("expense", 9999L, "餐饮", "food", "2026-08-20", "午饭"));

        BillUpdateDTO dto = new BillUpdateDTO();
        dto.setAmount(12345L);
        dto.setNote("修改后的备注");
        assertTrue(billService.updateBill(USER_A, created.getId(), dto));

        BillVO updated = billService.getBillById(USER_A, created.getId());
        assertEquals(12345L, updated.getAmount());
        assertEquals("修改后的备注", updated.getNote());
        // 未传入的字段保持不变
        assertEquals("expense", updated.getType());
        assertEquals("餐饮", updated.getCategory());
        assertEquals("2026-08-20", updated.getDate());
    }

    @Test
    @DisplayName("2.2 修改账单：清空备注")
    void updateBill_clearNote() {
        BillVO created = billService.createBill(USER_A,
                buildDTO("expense", 9999L, "餐饮", "food", "2026-08-20", "午饭"));

        BillUpdateDTO dto = new BillUpdateDTO();
        dto.setNote("");
        billService.updateBill(USER_A, created.getId(), dto);

        assertEquals("", billService.getBillById(USER_A, created.getId()).getNote());
    }

    @Test
    @DisplayName("2.2 修改账单：他人账单不可修改")
    void updateBill_notOwned() {
        BillVO created = billService.createBill(USER_A,
                buildDTO("expense", 9999L, "餐饮", "food", "2026-08-20", "午饭"));

        BillUpdateDTO dto = new BillUpdateDTO();
        dto.setAmount(1L);
        assertThrows(BusinessException.class, () -> billService.updateBill(USER_B, created.getId(), dto));
    }

    @Test
    @DisplayName("2.2 修改账单：账单不存在")
    void updateBill_notExist() {
        BillUpdateDTO dto = new BillUpdateDTO();
        dto.setAmount(1L);
        assertThrows(BusinessException.class, () -> billService.updateBill(USER_A, "b_not_exist", dto));
    }

    @Test
    @DisplayName("2.3 删除账单：逻辑删除后查不到，且他人数据不受影响")
    void deleteBill() {
        BillVO target = billService.createBill(USER_A,
                buildDTO("expense", 9999L, "餐饮", "food", "2026-08-20", "午饭"));
        BillVO other = billService.createBill(USER_A,
                buildDTO("expense", 1000L, "交通", "transport", "2026-08-20", "地铁"));

        assertTrue(billService.deleteBill(USER_A, target.getId()));
        assertThrows(BusinessException.class, () -> billService.getBillById(USER_A, target.getId()),
                "删除后详情应不可见");
        assertNotNull(billService.getBillById(USER_A, other.getId()), "其他账单不受影响");
        assertEquals(1L, billMapper.selectCount(null), "逻辑删除后库中仅剩 1 条正常记录");
    }

    @Test
    @DisplayName("2.3 删除账单：他人账单不可删除")
    void deleteBill_notOwned() {
        BillVO created = billService.createBill(USER_A,
                buildDTO("expense", 9999L, "餐饮", "food", "2026-08-20", "午饭"));
        assertThrows(BusinessException.class, () -> billService.deleteBill(USER_B, created.getId()));
    }

    @Test
    @DisplayName("2.4 获取单条账单详情")
    void getBillById() {
        BillVO created = billService.createBill(USER_A,
                buildDTO("income", 500000L, "工资", "salary", "2026-08-20", ""));

        BillVO detail = billService.getBillById(USER_A, created.getId());

        assertEquals(created.getId(), detail.getId());
        assertEquals("income", detail.getType());
        assertEquals(500000L, detail.getAmount());
    }

    @Test
    @DisplayName("2.5 月度账单列表：只返回当月数据，按日期+创建时间倒序")
    void getBillsByMonth() {
        BillVO aug1 = billService.createBill(USER_A,
                buildDTO("expense", 1000L, "餐饮", "food", "2026-08-01", "a"));
        BillVO aug20 = billService.createBill(USER_A,
                buildDTO("expense", 2000L, "交通", "transport", "2026-08-20", "b"));
        billService.createBill(USER_A,
                buildDTO("expense", 3000L, "购物", "shopping", "2026-07-31", "c"));
        billService.createBill(USER_B,
                buildDTO("expense", 4000L, "娱乐", "entertainment", "2026-08-20", "d"));

        List<BillVO> list = billService.getBillsByMonth(USER_A, "2026-08");

        assertEquals(2, list.size(), "只返回用户 A 的 8 月账单");
        assertEquals(aug20.getId(), list.get(0).getId(), "按日期倒序");
        assertEquals(aug1.getId(), list.get(1).getId());
    }

    @Test
    @DisplayName("2.5 月度账单列表：月份格式非法")
    void getBillsByMonth_invalidFormat() {
        assertThrows(BusinessException.class, () -> billService.getBillsByMonth(USER_A, "202608"));
    }

    @Test
    @DisplayName("2.6 月度汇总")
    void getSummary() {
        billService.createBill(USER_A, buildDTO("expense", 5000L, "餐饮", "food", "2026-08-10", null));
        billService.createBill(USER_A, buildDTO("expense", 10000L, "交通", "transport", "2026-08-11", null));
        billService.createBill(USER_A, buildDTO("income", 50000L, "工资", "salary", "2026-08-12", null));
        billService.createBill(USER_A, buildDTO("expense", 999L, "餐饮", "food", "2026-07-01", null));

        BillSummaryVO summary = billService.getSummary(USER_A, "2026-08");

        assertEquals(15000L, summary.getTotalExpense());
        assertEquals(50000L, summary.getTotalIncome());
        assertEquals(35000L, summary.getBalance());
    }

    @Test
    @DisplayName("2.7 日期范围查询：含边界，不含范围外")
    void getBillsByRange() {
        billService.createBill(USER_A, buildDTO("expense", 1000L, "餐饮", "food", "2026-08-01", null));
        billService.createBill(USER_A, buildDTO("expense", 2000L, "交通", "transport", "2026-08-10", null));
        billService.createBill(USER_A, buildDTO("expense", 3000L, "购物", "shopping", "2026-08-11", null));

        List<BillVO> list = billService.getBillsByRange(USER_A, "2026-08-01", "2026-08-10");

        assertEquals(2, list.size(), "边界日期应包含在内");
    }

    @Test
    @DisplayName("2.7 日期范围查询：开始日期晚于结束日期")
    void getBillsByRange_invalidRange() {
        assertThrows(BusinessException.class,
                () -> billService.getBillsByRange(USER_A, "2026-08-10", "2026-08-01"));
    }

    @Test
    @DisplayName("2.8 导出账单")
    void export() {
        billService.createBill(USER_A, buildDTO("expense", 1000L, "餐饮", "food", "2026-08-01", "a"));
        billService.createBill(USER_A, buildDTO("income", 2000L, "工资", "salary", "2026-08-02", "b"));
        billService.createBill(USER_B, buildDTO("expense", 3000L, "购物", "shopping", "2026-08-03", "c"));

        BillExportVO export = billService.export(USER_A);

        assertEquals("niuniu-account", export.getApp());
        assertEquals("1.0.0", export.getVersion());
        assertNotNull(export.getExportTime());
        assertEquals(2, export.getBills().size(), "只导出当前用户的账单");
    }

    @Test
    @DisplayName("2.9 导入账单：去重 + 字段校验 + 类型归一化")
    void importBills() {
        BillVO existed = billService.createBill(USER_A,
                buildDTO("expense", 9999L, "餐饮", "food", "2026-08-20", "已存在"));

        BillImportItemDTO dup = item("b_dup", "expense", 100L, "餐饮", "food", "2026-08-01", null);
        // 与库中已存在的 ID 重复
        BillImportItemDTO dupExist = item(existed.getId(), "expense", 9999L, "餐饮", "food", "2026-08-20", null);
        // 字段不完整（缺 date）
        BillImportItemDTO invalid = item("b_invalid", "expense", 100L, "餐饮", "food", null, null);
        // type 非 income 归为 expense
        BillImportItemDTO weirdType = item("b_weird", "other", 200L, "餐饮", "food", "2026-08-02", null);
        // 合法收入
        BillImportItemDTO income = item("b_income", "income", 300L, "工资", "salary", "2026-08-03", null);
        income.setCreateTime(1720000000000L);

        // dup 在同批次出现两次，第二次应被去重跳过
        ImportResultVO result = billService.importBills(USER_A,
                List.of(dup, dup, dupExist, invalid, weirdType, income));

        assertEquals(3, result.getImportedCount(), "应导入 3 条（dup 首次 + weirdType + income）");
        assertEquals(3, result.getSkippedCount(), "应跳过 3 条（同批次重复 1 + 库中重复 1 + 字段不完整 1）");
        // 类型归一化验证
        assertEquals("expense", billService.getBillById(USER_A, "b_weird").getType());
        assertEquals("income", billService.getBillById(USER_A, "b_income").getType());
        // 导入保留原 createTime
        assertEquals(1720000000000L, billService.getBillById(USER_A, "b_income").getCreateTime());
        // 重复 ID 未被覆盖
        assertEquals(9999L, billService.getBillById(USER_A, existed.getId()).getAmount());
    }

    private BillImportItemDTO item(String id, String type, long amount,
                                   String category, String icon, String date, String note) {
        BillImportItemDTO item = new BillImportItemDTO();
        item.setId(id);
        item.setType(type);
        item.setAmount(amount);
        item.setCategory(category);
        item.setCategoryIcon(icon);
        item.setDate(date);
        item.setNote(note);
        return item;
    }
}

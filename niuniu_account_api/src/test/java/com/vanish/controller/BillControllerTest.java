package com.vanish.controller;

import com.vanish.NiuniuAccountApplication;
import com.vanish.common.util.JwtUtil;
import com.vanish.service.IBillService;
import com.vanish.service.dto.BillDTO;
import com.vanish.service.dto.BillImportItemDTO;
import com.vanish.service.vo.BillExportVO;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.BillVO;
import com.vanish.service.vo.ImportResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 账单模块接口测试
 */
@SpringBootTest(classes = NiuniuAccountApplication.class)
@AutoConfigureMockMvc
class BillControllerTest {

    private static final String BILL_JSON = """
            {
              "type": "expense",
              "amount": 9999,
              "category": "餐饮",
              "categoryIcon": "food",
              "date": "2026-08-20",
              "note": "午饭"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private IBillService billService;

    private String authHeader;

    private BillVO buildBillVO() {
        return BillVO.builder()
                .id("b_1689897600_abc123")
                .type("expense")
                .amount(9999L)
                .category("餐饮")
                .categoryIcon("food")
                .date("2026-08-20")
                .note("午饭")
                .createTime(1689897600000L)
                .build();
    }

    @BeforeEach
    void setUp() {
        authHeader = "Bearer " + jwtUtil.generateToken("u_test");
    }

    @Test
    @DisplayName("2.1 POST /api/bill：新建账单")
    void create() throws Exception {
        when(billService.createBill(anyString(), any(BillDTO.class))).thenReturn(buildBillVO());

        mockMvc.perform(post("/api/bill")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BILL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("b_1689897600_abc123"))
                .andExpect(jsonPath("$.data.amount").value(9999))
                .andExpect(jsonPath("$.data.createTime").value(1689897600000L));
    }

    @Test
    @DisplayName("2.1 POST /api/bill：金额缺失返回参数校验错误")
    void create_invalidParam() throws Exception {
        mockMvc.perform(post("/api/bill")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"expense\",\"category\":\"餐饮\",\"categoryIcon\":\"food\",\"date\":\"2026-08-20\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("金额不能为空"));
    }

    @Test
    @DisplayName("2.1 POST /api/bill：类型非法返回参数校验错误")
    void create_invalidType() throws Exception {
        mockMvc.perform(post("/api/bill")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"other\",\"amount\":1,\"category\":\"餐饮\",\"categoryIcon\":\"food\",\"date\":\"2026-08-20\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("2.2 PUT /api/bill/{id}：修改账单")
    void update() throws Exception {
        when(billService.updateBill(anyString(), eq("b_1"), any())).thenReturn(true);

        mockMvc.perform(put("/api/bill/b_1")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":12345,\"note\":\"改\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("修改成功"));
    }

    @Test
    @DisplayName("2.3 DELETE /api/bill/{id}：删除账单")
    void deleteBill() throws Exception {
        when(billService.deleteBill(anyString(), eq("b_1"))).thenReturn(true);

        mockMvc.perform(delete("/api/bill/b_1").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @DisplayName("2.4 GET /api/bill/{id}：账单详情")
    void detail() throws Exception {
        when(billService.getBillById(anyString(), eq("b_1"))).thenReturn(buildBillVO());

        mockMvc.perform(get("/api/bill/b_1").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.category").value("餐饮"));
    }

    @Test
    @DisplayName("2.5 GET /api/bill/month/{month}：月度账单列表")
    void month() throws Exception {
        when(billService.getBillsByMonth(anyString(), eq("2026-08")))
                .thenReturn(List.of(buildBillVO()));

        mockMvc.perform(get("/api/bill/month/2026-08").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("2.6 GET /api/bill/summary：月度汇总")
    void summary() throws Exception {
        when(billService.getSummary(anyString(), eq("2026-08")))
                .thenReturn(BillSummaryVO.builder()
                        .totalExpense(15000L).totalIncome(50000L).balance(35000L).build());

        mockMvc.perform(get("/api/bill/summary").param("month", "2026-08")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalExpense").value(15000))
                .andExpect(jsonPath("$.data.balance").value(35000));
    }

    @Test
    @DisplayName("2.7 GET /api/bill/range：日期范围账单列表")
    void range() throws Exception {
        when(billService.getBillsByRange(anyString(), eq("2026-08-01"), eq("2026-08-31")))
                .thenReturn(List.of(buildBillVO()));

        mockMvc.perform(get("/api/bill/range")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("2.8 GET /api/bill/export：导出账单")
    void export() throws Exception {
        when(billService.export(anyString()))
                .thenReturn(BillExportVO.builder()
                        .app("niuniu-account")
                        .version("1.0.0")
                        .exportTime(1689897600000L)
                        .bills(List.of(buildBillVO()))
                        .build());

        mockMvc.perform(get("/api/bill/export").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.app").value("niuniu-account"))
                .andExpect(jsonPath("$.data.bills.length()").value(1));
    }

    @Test
    @DisplayName("2.9 POST /api/bill/import：纯数组格式导入")
    void importArray() throws Exception {
        when(billService.importBills(anyString(), anyList()))
                .thenReturn(ImportResultVO.builder().importedCount(1).skippedCount(0).build());

        mockMvc.perform(post("/api/bill/import")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"id\":\"b_1\",\"type\":\"expense\",\"amount\":100,\"category\":\"餐饮\",\"date\":\"2026-08-01\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.importedCount").value(1))
                .andExpect(jsonPath("$.data.skippedCount").value(0));
    }

    @Test
    @DisplayName("2.9 POST /api/bill/import：{bills:[...]} 对象格式导入")
    void importObjectFormat() throws Exception {
        when(billService.importBills(anyString(), anyList()))
                .thenReturn(ImportResultVO.builder().importedCount(1).skippedCount(0).build());

        mockMvc.perform(post("/api/bill/import")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"app\":\"niuniu-account\",\"bills\":[{\"id\":\"b_1\",\"type\":\"expense\",\"amount\":100,\"category\":\"餐饮\",\"date\":\"2026-08-01\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.importedCount").value(1));
    }

    @Test
    @DisplayName("2.9 POST /api/bill/import：格式非法返回业务错误")
    void importInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/bill/import")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"foo\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}

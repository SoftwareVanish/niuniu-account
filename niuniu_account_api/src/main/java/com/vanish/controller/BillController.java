package com.vanish.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.vanish.common.exception.BusinessException;
import com.vanish.common.result.ResultVO;
import com.vanish.service.IBillService;
import com.vanish.service.dto.BillDTO;
import com.vanish.service.dto.BillImportItemDTO;
import com.vanish.service.dto.BillUpdateDTO;
import com.vanish.service.vo.BillExportVO;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.BillVO;
import com.vanish.service.vo.ImportResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 账单模块接口
 */
@RestController
@RequestMapping("/api/bill")
@RequiredArgsConstructor
public class BillController {

    private final IBillService billService;
    private final ObjectMapper objectMapper;

    /**
     * 2.1 新建账单
     */
    @PostMapping
    public ResultVO<BillVO> create(@RequestAttribute("userId") String userId,
                                   @Valid @RequestBody BillDTO dto) {
        return ResultVO.successWithData(billService.createBill(userId, dto));
    }

    /**
     * 2.2 修改账单
     */
    @PutMapping("/{id}")
    public ResultVO<Void> update(@RequestAttribute("userId") String userId,
                                 @PathVariable("id") String id,
                                 @Valid @RequestBody BillUpdateDTO dto) {
        billService.updateBill(userId, id, dto);
        return ResultVO.successWithMessage("修改成功");
    }

    /**
     * 2.3 删除账单
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> delete(@RequestAttribute("userId") String userId,
                                 @PathVariable("id") String id) {
        billService.deleteBill(userId, id);
        return ResultVO.successWithMessage("删除成功");
    }

    /**
     * 2.4 获取单条账单详情
     */
    @GetMapping("/{id}")
    public ResultVO<BillVO> detail(@RequestAttribute("userId") String userId,
                                   @PathVariable("id") String id) {
        return ResultVO.successWithData(billService.getBillById(userId, id));
    }

    /**
     * 2.5 获取月度账单列表
     */
    @GetMapping("/month/{month}")
    public ResultVO<List<BillVO>> month(@RequestAttribute("userId") String userId,
                                        @PathVariable("month") String month) {
        return ResultVO.successWithData(billService.getBillsByMonth(userId, month));
    }

    /**
     * 2.6 获取月度汇总
     */
    @GetMapping("/summary")
    public ResultVO<BillSummaryVO> summary(@RequestAttribute("userId") String userId,
                                           @RequestParam("month") String month) {
        return ResultVO.successWithData(billService.getSummary(userId, month));
    }

    /**
     * 2.7 获取日期范围内账单列表
     */
    @GetMapping("/range")
    public ResultVO<List<BillVO>> range(@RequestAttribute("userId") String userId,
                                        @RequestParam("startDate") String startDate,
                                        @RequestParam("endDate") String endDate) {
        return ResultVO.successWithData(billService.getBillsByRange(userId, startDate, endDate));
    }

    /**
     * 2.8 导出账单数据
     */
    @GetMapping("/export")
    public ResultVO<BillExportVO> export(@RequestAttribute("userId") String userId) {
        return ResultVO.successWithData(billService.export(userId));
    }

    /**
     * 2.9 导入账单数据（兼容 {bills:[...]} 对象格式和纯数组格式）
     */
    @PostMapping("/import")
    public ResultVO<ImportResultVO> importBills(@RequestAttribute("userId") String userId,
                                                @RequestBody JsonNode body) {
        JsonNode arrayNode = parseImportBody(body);
        List<BillImportItemDTO> items = new ArrayList<>();
        for (JsonNode itemNode : arrayNode) {
            try {
                items.add(objectMapper.treeToValue(itemNode, BillImportItemDTO.class));
            } catch (Exception e) {
                throw new BusinessException("导入数据格式不正确");
            }
        }
        return ResultVO.successWithData(billService.importBills(userId, items));
    }

    /**
     * 解析导入请求体：数组格式直接用，对象格式取 bills 字段
     */
    private JsonNode parseImportBody(JsonNode body) {
        if (body != null && body.isArray()) {
            return body;
        }
        if (body != null && body.isObject() && body.has("bills") && body.get("bills").isArray()) {
            return body.get("bills");
        }
        throw new BusinessException("导入数据格式不正确");
    }
}

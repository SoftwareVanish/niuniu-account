package com.vanish.service;

import com.vanish.service.dto.BillDTO;
import com.vanish.service.dto.BillImportItemDTO;
import com.vanish.service.dto.BillUpdateDTO;
import com.vanish.service.vo.BillExportVO;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.BillVO;
import com.vanish.service.vo.ImportResultVO;

import java.util.List;

/**
 * 账单服务接口
 */
public interface IBillService {

    /**
     * 新建账单
     *
     * @param userId 当前登录用户 ID
     * @param dto    账单参数
     * @return 新建后的账单
     */
    BillVO createBill(String userId, BillDTO dto);

    /**
     * 修改账单（仅更新传入字段）
     *
     * @param userId 当前登录用户 ID
     * @param id     账单 ID
     * @param dto    更新参数
     * @return 是否修改成功
     */
    boolean updateBill(String userId, String id, BillUpdateDTO dto);

    /**
     * 删除账单（逻辑删除）
     *
     * @param userId 当前登录用户 ID
     * @param id     账单 ID
     * @return 是否删除成功
     */
    boolean deleteBill(String userId, String id);

    /**
     * 获取单条账单详情
     *
     * @param userId 当前登录用户 ID
     * @param id     账单 ID
     * @return 账单详情
     */
    BillVO getBillById(String userId, String id);

    /**
     * 获取月度账单列表（按日期倒序、组内按创建时间倒序）
     *
     * @param userId 当前登录用户 ID
     * @param month  月份 YYYY-MM
     * @return 账单列表
     */
    List<BillVO> getBillsByMonth(String userId, String month);

    /**
     * 获取月度汇总（总支出 / 总收入 / 结余）
     *
     * @param userId 当前登录用户 ID
     * @param month  月份 YYYY-MM
     * @return 月度汇总
     */
    BillSummaryVO getSummary(String userId, String month);

    /**
     * 获取日期范围内账单列表
     *
     * @param userId    当前登录用户 ID
     * @param startDate 开始日期 YYYY-MM-DD
     * @param endDate   结束日期 YYYY-MM-DD
     * @return 账单列表
     */
    List<BillVO> getBillsByRange(String userId, String startDate, String endDate);

    /**
     * 导出当前用户全部账单
     *
     * @param userId 当前登录用户 ID
     * @return 导出数据
     */
    BillExportVO export(String userId);

    /**
     * 导入账单（字段校验 + 按 ID 去重，type 非 income 归为 expense）
     *
     * @param userId 当前登录用户 ID
     * @param items  导入条目
     * @return 导入结果（成功数 / 跳过数）
     */
    ImportResultVO importBills(String userId, List<BillImportItemDTO> items);
}

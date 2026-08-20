package com.vanish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vanish.common.exception.BusinessException;
import com.vanish.common.util.IdGenerator;
import com.vanish.dao.entity.Bill;
import com.vanish.dao.mapper.BillMapper;
import com.vanish.service.IBillService;
import com.vanish.service.dto.BillDTO;
import com.vanish.service.dto.BillImportItemDTO;
import com.vanish.service.dto.BillUpdateDTO;
import com.vanish.service.vo.BillExportVO;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.BillVO;
import com.vanish.service.vo.ImportResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 账单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements IBillService {

    private static final String MONTH_PATTERN = "\\d{4}-\\d{2}";
    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
    private static final String APP_NAME = "niuniu-account";
    private static final String APP_VERSION = "1.0.0";

    private final BillMapper billMapper;

    @Override
    public BillVO createBill(String userId, BillDTO dto) {
        Bill bill = new Bill();
        bill.setId(IdGenerator.next("b"));
        bill.setUserId(userId);
        bill.setType(dto.getType());
        bill.setAmount(dto.getAmount());
        bill.setCategory(dto.getCategory());
        bill.setCategoryIcon(dto.getCategoryIcon());
        bill.setDate(dto.getDate());
        bill.setNote(dto.getNote());
        bill.setCreateBy(userId);
        billMapper.insert(bill);
        // 重新查询，回填数据库默认值（createTime 等）
        bill = billMapper.selectById(bill.getId());
        log.info("BillServiceImpl.createBill | success | billId:{} | userId:{}", bill.getId(), userId);
        return convertToVO(bill);
    }

    @Override
    public boolean updateBill(String userId, String id, BillUpdateDTO dto) {
        Bill exist = getOwnedBill(userId, id);
        Bill update = new Bill();
        update.setId(exist.getId());
        if (dto.getType() != null) {
            update.setType(dto.getType());
        }
        if (dto.getAmount() != null) {
            update.setAmount(dto.getAmount());
        }
        if (dto.getCategory() != null) {
            update.setCategory(dto.getCategory());
        }
        if (dto.getCategoryIcon() != null) {
            update.setCategoryIcon(dto.getCategoryIcon());
        }
        if (dto.getDate() != null) {
            update.setDate(dto.getDate());
        }
        if (dto.getNote() != null) {
            update.setNote(dto.getNote());
        }
        update.setUpdateBy(userId);
        int rows = billMapper.updateById(update);
        log.info("BillServiceImpl.updateBill | billId:{} | userId:{} | rows:{}", id, userId, rows);
        return rows > 0;
    }

    @Override
    public boolean deleteBill(String userId, String id) {
        getOwnedBill(userId, id);
        int rows = billMapper.deleteById(id);
        log.info("BillServiceImpl.deleteBill | billId:{} | userId:{} | rows:{}", id, userId, rows);
        return rows > 0;
    }

    @Override
    public BillVO getBillById(String userId, String id) {
        return convertToVO(getOwnedBill(userId, id));
    }

    @Override
    public List<BillVO> getBillsByMonth(String userId, String month) {
        checkPattern(month, MONTH_PATTERN, "月份格式必须为 YYYY-MM");
        return listByWrapper(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getUserId, userId)
                .likeRight(Bill::getDate, month)
                .orderByDesc(Bill::getDate)
                .orderByDesc(Bill::getCreateTime));
    }

    @Override
    public BillSummaryVO getSummary(String userId, String month) {
        checkPattern(month, MONTH_PATTERN, "月份格式必须为 YYYY-MM");
        List<Bill> bills = billMapper.selectList(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getUserId, userId)
                .likeRight(Bill::getDate, month));
        return summarize(bills);
    }

    @Override
    public List<BillVO> getBillsByRange(String userId, String startDate, String endDate) {
        checkPattern(startDate, DATE_PATTERN, "开始日期格式必须为 YYYY-MM-DD");
        checkPattern(endDate, DATE_PATTERN, "结束日期格式必须为 YYYY-MM-DD");
        if (startDate.compareTo(endDate) > 0) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        return listByWrapper(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getUserId, userId)
                .between(Bill::getDate, startDate, endDate)
                .orderByDesc(Bill::getDate)
                .orderByDesc(Bill::getCreateTime));
    }

    @Override
    public BillExportVO export(String userId) {
        List<BillVO> bills = listByWrapper(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getUserId, userId)
                .orderByAsc(Bill::getDate)
                .orderByAsc(Bill::getCreateTime));
        return BillExportVO.builder()
                .app(APP_NAME)
                .version(APP_VERSION)
                .exportTime(System.currentTimeMillis())
                .bills(bills)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importBills(String userId, List<BillImportItemDTO> items) {
        int imported = 0;
        int skipped = 0;

        // 先剔除条目内重复 ID（同一次导入里出现多次的只算一次）
        Set<String> seenIds = new HashSet<>();
        List<BillImportItemDTO> validItems = new java.util.ArrayList<>();
        for (BillImportItemDTO item : items) {
            if (!isValid(item)) {
                skipped++;
                continue;
            }
            if (item.getId() != null && !seenIds.add(item.getId())) {
                skipped++;
                continue;
            }
            validItems.add(item);
        }

        // 再按数据库已有 ID 去重
        Set<String> existIds = new HashSet<>();
        if (!validItems.isEmpty()) {
            List<String> ids = validItems.stream().map(BillImportItemDTO::getId).toList();
            billMapper.selectByIds(ids).forEach(b -> existIds.add(b.getId()));
        }

        for (BillImportItemDTO item : validItems) {
            if (existIds.contains(item.getId())) {
                skipped++;
                continue;
            }
            Bill bill = new Bill();
            bill.setId(item.getId());
            bill.setUserId(userId);
            // 对齐前端逻辑：type 非 income 一律归为 expense
            bill.setType("income".equals(item.getType()) ? "income" : "expense");
            bill.setAmount(item.getAmount());
            bill.setCategory(item.getCategory());
            bill.setCategoryIcon(item.getCategoryIcon());
            bill.setDate(item.getDate());
            bill.setNote(item.getNote());
            // 导入数据保留原创建时间（毫秒时间戳）
            if (item.getCreateTime() != null) {
                bill.setCreateTime(new Date(item.getCreateTime()));
            }
            bill.setCreateBy(userId);
            billMapper.insert(bill);
            imported++;
        }
        log.info("BillServiceImpl.importBills | userId:{} | imported:{} | skipped:{}",
                userId, imported, skipped);
        return ImportResultVO.builder()
                .importedCount(imported)
                .skippedCount(skipped)
                .build();
    }

    /**
     * 查询当前用户的账单，不存在或不属于该用户时抛业务异常
     */
    private Bill getOwnedBill(String userId, String id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (!Objects.equals(bill.getUserId(), userId)) {
            throw new BusinessException("账单不存在");
        }
        return bill;
    }

    /**
     * 按条件查询并转换为 VO
     */
    private List<BillVO> listByWrapper(LambdaQueryWrapper<Bill> wrapper) {
        return billMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 计算总支出 / 总收入 / 结余
     */
    private BillSummaryVO summarize(List<Bill> bills) {
        Map<String, Long> totalByType = bills.stream()
                .collect(Collectors.groupingBy(Bill::getType,
                        Collectors.summingLong(Bill::getAmount)));
        long totalExpense = totalByType.getOrDefault("expense", 0L);
        long totalIncome = totalByType.getOrDefault("income", 0L);
        return BillSummaryVO.builder()
                .totalExpense(totalExpense)
                .totalIncome(totalIncome)
                .balance(totalIncome - totalExpense)
                .build();
    }

    /**
     * 导入条目字段完整性校验（对齐前端：id、type、amount、category、date 必填）
     */
    private boolean isValid(BillImportItemDTO item) {
        return item != null
                && item.getId() != null && !item.getId().isBlank()
                && item.getType() != null && !item.getType().isBlank()
                && item.getAmount() != null && item.getAmount() > 0
                && item.getCategory() != null && !item.getCategory().isBlank()
                && item.getDate() != null && item.getDate().matches(DATE_PATTERN);
    }

    /**
     * 格式校验
     */
    private void checkPattern(String value, String pattern, String message) {
        if (value == null || !value.matches(pattern)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 实体转 VO（createTime 转毫秒时间戳）
     */
    private BillVO convertToVO(Bill bill) {
        return BillVO.builder()
                .id(bill.getId())
                .type(bill.getType())
                .amount(bill.getAmount())
                .category(bill.getCategory())
                .categoryIcon(bill.getCategoryIcon())
                .date(bill.getDate())
                .note(bill.getNote())
                .createTime(bill.getCreateTime() == null ? null : bill.getCreateTime().getTime())
                .build();
    }
}

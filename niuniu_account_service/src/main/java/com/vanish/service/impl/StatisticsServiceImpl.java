package com.vanish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vanish.common.exception.BusinessException;
import com.vanish.dao.entity.Bill;
import com.vanish.dao.mapper.BillMapper;
import com.vanish.service.IStatisticsService;
import com.vanish.service.vo.BillSummaryVO;
import com.vanish.service.vo.CategoryStatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    private final BillMapper billMapper;

    @Override
    public BillSummaryVO summary(String userId, String startDate, String endDate, String type) {
        List<Bill> bills = listRangeBills(userId, startDate, endDate, type);
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

    @Override
    public List<CategoryStatVO> categoryStats(String userId, String startDate, String endDate, String type) {
        List<Bill> bills = listRangeBills(userId, startDate, endDate, type);

        // 按 type+category 分组，累计金额，组内取一个图标
        record GroupAgg(long amount, String categoryIcon) {
        }
        Map<String, GroupAgg> grouped = bills.stream()
                .collect(Collectors.groupingBy(
                        bill -> bill.getType() + "_" + bill.getCategory(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new GroupAgg(
                                        list.stream().mapToLong(Bill::getAmount).sum(),
                                        list.getFirst().getCategoryIcon()))));

        // 占比分母：all -> 总支出 + 总收入；expense -> 总支出；income -> 总收入
        long denominator = grouped.entrySet().stream()
                .filter(e -> "all".equals(type) || e.getKey().startsWith(type + "_"))
                .mapToLong(e -> e.getValue().amount())
                .sum();

        return grouped.entrySet().stream()
                .map(e -> {
                    String key = e.getKey();
                    String billType = key.substring(0, key.indexOf('_'));
                    String category = key.substring(key.indexOf('_') + 1);
                    double percentage = denominator == 0 ? 0.0
                            : BigDecimal.valueOf(e.getValue().amount() * 100.0 / denominator)
                            .setScale(1, RoundingMode.HALF_UP)
                            .doubleValue();
                    return CategoryStatVO.builder()
                            .key(key)
                            .type(billType)
                            .category(category)
                            .categoryIcon(e.getValue().categoryIcon())
                            .amount(e.getValue().amount())
                            .percentage(percentage)
                            .build();
                })
                .sorted(Comparator.comparingLong(CategoryStatVO::getAmount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 查询日期范围内（可按类型过滤）的账单
     */
    private List<Bill> listRangeBills(String userId, String startDate, String endDate, String type) {
        checkDate(startDate, "开始日期格式必须为 YYYY-MM-DD");
        checkDate(endDate, "结束日期格式必须为 YYYY-MM-DD");
        if (startDate.compareTo(endDate) > 0) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (type == null || type.isBlank()) {
            type = "all";
        }
        if (!"all".equals(type) && !"expense".equals(type) && !"income".equals(type)) {
            throw new BusinessException("筛选类型只能是 all、expense 或 income");
        }
        return billMapper.selectList(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getUserId, userId)
                .eq(!"all".equals(type), Bill::getType, type)
                .between(Bill::getDate, startDate, endDate));
    }

    /**
     * 日期格式校验
     */
    private void checkDate(String value, String message) {
        if (value == null || !value.matches(DATE_PATTERN)) {
            throw new BusinessException(message);
        }
    }
}

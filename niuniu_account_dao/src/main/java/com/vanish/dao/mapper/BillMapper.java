package com.vanish.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vanish.dao.entity.Bill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单 Mapper（单表查询走 BaseMapper + LambdaWrapper）
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}

package com.vanish.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vanish.dao.entity.CustomCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自定义分类 Mapper（单表查询走 BaseMapper + LambdaWrapper）
 */
@Mapper
public interface CustomCategoryMapper extends BaseMapper<CustomCategory> {
}

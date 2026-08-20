package com.vanish.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vanish.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（单表查询走 BaseMapper + LambdaWrapper）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

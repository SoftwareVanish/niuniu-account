package com.vanish.service;

import com.vanish.service.dto.CategoryDTO;
import com.vanish.service.dto.CategoryUpdateDTO;
import com.vanish.service.vo.CategoryListVO;
import com.vanish.service.vo.CategoryVO;

/**
 * 分类服务接口
 */
public interface ICategoryService {

    /**
     * 获取分类列表（系统预设 + 当前用户自定义）
     *
     * @param userId 当前登录用户 ID
     * @param type   分类类型：expense 支出 / income 收入
     * @return 预设 + 自定义分类列表
     */
    CategoryListVO list(String userId, String type);

    /**
     * 新增自定义分类（同类型下与预设 + 自定义重名校验）
     *
     * @param userId 当前登录用户 ID
     * @param dto    分类参数
     * @return 新增后的分类
     */
    CategoryVO add(String userId, CategoryDTO dto);

    /**
     * 修改自定义分类（仅更新传入字段）
     *
     * @param userId 当前登录用户 ID
     * @param id     分类 ID
     * @param dto    更新参数
     * @return 是否修改成功
     */
    boolean update(String userId, String id, CategoryUpdateDTO dto);

    /**
     * 删除自定义分类（逻辑删除，历史账单不受影响）
     *
     * @param userId 当前登录用户 ID
     * @param id     分类 ID
     * @param type   分类类型
     * @return 是否删除成功
     */
    boolean delete(String userId, String id, String type);
}

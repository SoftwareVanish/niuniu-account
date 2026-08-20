package com.vanish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vanish.common.constant.PresetCategory;
import com.vanish.common.exception.BusinessException;
import com.vanish.common.util.IdGenerator;
import com.vanish.dao.entity.CustomCategory;
import com.vanish.dao.mapper.CustomCategoryMapper;
import com.vanish.service.ICategoryService;
import com.vanish.service.dto.CategoryDTO;
import com.vanish.service.dto.CategoryUpdateDTO;
import com.vanish.service.vo.CategoryListVO;
import com.vanish.service.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CustomCategoryMapper customCategoryMapper;

    @Override
    public CategoryListVO list(String userId, String type) {
        checkType(type);
        List<CategoryVO> preset = PresetCategory.listByType(type).stream()
                .map(c -> CategoryVO.builder().name(c.getName()).icon(c.getIcon()).build())
                .collect(java.util.stream.Collectors.toList());
        List<CategoryVO> custom = customCategoryMapper.selectList(new LambdaQueryWrapper<CustomCategory>()
                        .eq(CustomCategory::getUserId, userId)
                        .eq(CustomCategory::getType, type)
                        .orderByAsc(CustomCategory::getCreateTime))
                .stream()
                .map(c -> CategoryVO.builder().id(c.getId()).name(c.getName()).icon(c.getIcon()).build())
                .collect(java.util.stream.Collectors.toList());
        return CategoryListVO.builder().preset(preset).custom(custom).build();
    }

    @Override
    public CategoryVO add(String userId, CategoryDTO dto) {
        checkType(dto.getType());
        checkDuplicate(userId, dto.getType(), dto.getName(), null);
        CustomCategory category = new CustomCategory();
        category.setId(IdGenerator.next("c"));
        category.setUserId(userId);
        category.setType(dto.getType());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setCreateBy(userId);
        customCategoryMapper.insert(category);
        log.info("CategoryServiceImpl.add | success | categoryId:{} | userId:{}", category.getId(), userId);
        return CategoryVO.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .build();
    }

    @Override
    public boolean update(String userId, String id, CategoryUpdateDTO dto) {
        checkType(dto.getType());
        CustomCategory exist = getOwnedCategory(userId, id, dto.getType());
        if (dto.getName() != null) {
            checkDuplicate(userId, dto.getType(), dto.getName(), exist.getId());
        }
        CustomCategory update = new CustomCategory();
        update.setId(exist.getId());
        if (dto.getName() != null) {
            update.setName(dto.getName());
        }
        if (dto.getIcon() != null) {
            update.setIcon(dto.getIcon());
        }
        update.setUpdateBy(userId);
        int rows = customCategoryMapper.updateById(update);
        log.info("CategoryServiceImpl.update | categoryId:{} | userId:{} | rows:{}", id, userId, rows);
        return rows > 0;
    }

    @Override
    public boolean delete(String userId, String id, String type) {
        checkType(type);
        getOwnedCategory(userId, id, type);
        int rows = customCategoryMapper.deleteById(id);
        log.info("CategoryServiceImpl.delete | categoryId:{} | userId:{} | rows:{}", id, userId, rows);
        return rows > 0;
    }

    /**
     * 查询当前用户的自定义分类，不存在或不属于该用户时抛业务异常
     */
    private CustomCategory getOwnedCategory(String userId, String id, String type) {
        CustomCategory category = customCategoryMapper.selectById(id);
        if (category == null
                || !Objects.equals(category.getUserId(), userId)
                || !Objects.equals(category.getType(), type)) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    /**
     * 重名校验：同类型下与预设分类 + 当前用户自定义分类不重复（编辑时排除自身）
     */
    private void checkDuplicate(String userId, String type, String name, String excludeId) {
        if (PresetCategory.existsName(type, name)) {
            throw new BusinessException("分类名称与预设分类重复");
        }
        Long count = customCategoryMapper.selectCount(new LambdaQueryWrapper<CustomCategory>()
                .eq(CustomCategory::getUserId, userId)
                .eq(CustomCategory::getType, type)
                .eq(CustomCategory::getName, name)
                .ne(excludeId != null, CustomCategory::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException("分类名称已存在");
        }
    }

    /**
     * 分类类型校验
     */
    private void checkType(String type) {
        if (!"expense".equals(type) && !"income".equals(type)) {
            throw new BusinessException("分类类型只能是 expense 或 income");
        }
    }
}

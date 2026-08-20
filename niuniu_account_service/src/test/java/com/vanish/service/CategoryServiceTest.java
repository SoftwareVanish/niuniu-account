package com.vanish.service;

import com.vanish.common.exception.BusinessException;
import com.vanish.dao.mapper.CustomCategoryMapper;
import com.vanish.service.dto.CategoryDTO;
import com.vanish.service.dto.CategoryUpdateDTO;
import com.vanish.service.vo.CategoryListVO;
import com.vanish.service.vo.CategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分类服务单元测试（真实 MySQL 测试库）
 */
@SpringBootTest(classes = ServiceTestApplication.class)
class CategoryServiceTest {

    private static final String USER_A = "u_test_a";
    private static final String USER_B = "u_test_b";

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private CustomCategoryMapper customCategoryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.update("DELETE FROM t_bill");
        jdbcTemplate.update("DELETE FROM t_custom_category");
        jdbcTemplate.update("DELETE FROM t_user");
    }

    private CategoryDTO buildDTO(String type, String name, String icon) {
        CategoryDTO dto = new CategoryDTO();
        dto.setType(type);
        dto.setName(name);
        dto.setIcon(icon);
        return dto;
    }

    @Test
    @DisplayName("3.1 分类列表：预设分类数量正确，自定义分类隔离")
    void list() {
        // 支出预设 8 项
        CategoryListVO expense = categoryService.list(USER_A, "expense");
        assertEquals(8, expense.getPreset().size(), "支出预设分类应为 8 项");
        assertTrue(expense.getPreset().stream().allMatch(c -> c.getId() == null), "预设分类无 id");
        assertTrue(expense.getCustom().isEmpty(), "初始无自定义分类");
        assertTrue(expense.getPreset().stream().anyMatch(c -> "餐饮".equals(c.getName()) && "food".equals(c.getIcon())));

        // 收入预设 5 项
        CategoryListVO income = categoryService.list(USER_A, "income");
        assertEquals(5, income.getPreset().size(), "收入预设分类应为 5 项");

        // 用户 A 新增自定义分类，用户 B 不可见
        categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));
        assertEquals(1, categoryService.list(USER_A, "expense").getCustom().size());
        assertTrue(categoryService.list(USER_B, "expense").getCustom().isEmpty(), "自定义分类按用户隔离");
        // 预设数量不受影响
        assertEquals(8, categoryService.list(USER_A, "expense").getPreset().size());
    }

    @Test
    @DisplayName("3.1 分类列表：类型非法")
    void list_invalidType() {
        assertThrows(BusinessException.class, () -> categoryService.list(USER_A, "other"));
    }

    @Test
    @DisplayName("3.2 新增自定义分类")
    void add() {
        CategoryVO vo = categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));

        assertTrue(vo.getId().startsWith("c_"), "分类 ID 应以 c_ 开头");
        assertEquals("宠物", vo.getName());
        assertEquals("pet", vo.getIcon());
        assertNotNull(customCategoryMapper.selectById(vo.getId()), "分类应已入库");
    }

    @Test
    @DisplayName("3.2 新增自定义分类：与预设重名被拒绝")
    void add_duplicatePreset() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> categoryService.add(USER_A, buildDTO("expense", "餐饮", "food")));
        assertTrue(e.getMessage().contains("预设"));
    }

    @Test
    @DisplayName("3.2 新增自定义分类：同名自定义分类被拒绝")
    void add_duplicateCustom() {
        categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));
        assertThrows(BusinessException.class,
                () -> categoryService.add(USER_A, buildDTO("expense", "宠物", "pet2")));
    }

    @Test
    @DisplayName("3.2 新增自定义分类：不同用户或不同类型允许同名")
    void add_sameNameDifferentScope() {
        categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));
        // 不同用户同名 OK
        assertNotNull(categoryService.add(USER_B, buildDTO("expense", "宠物", "pet")));
        // 同一用户不同类型同名 OK（预设分类中收入没有"宠物"）
        assertNotNull(categoryService.add(USER_A, buildDTO("income", "宠物", "pet")));
    }

    @Test
    @DisplayName("3.3 修改自定义分类：只更新传入字段，编辑排除自身")
    void update() {
        CategoryVO created = categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));

        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setType("expense");
        dto.setName("宠物用品");
        assertTrue(categoryService.update(USER_A, created.getId(), dto));

        CategoryListVO list = categoryService.list(USER_A, "expense");
        assertEquals("宠物用品", list.getCustom().get(0).getName());
        assertEquals("pet", list.getCustom().get(0).getIcon(), "未传 icon 保持原值");

        // 改成自己的原名（排除自身）不报错
        CategoryUpdateDTO sameName = new CategoryUpdateDTO();
        sameName.setType("expense");
        sameName.setName("宠物用品");
        assertTrue(categoryService.update(USER_A, created.getId(), sameName));
    }

    @Test
    @DisplayName("3.3 修改自定义分类：改成与其他分类重名被拒绝")
    void update_duplicate() {
        categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));
        CategoryVO second = categoryService.add(USER_A, buildDTO("expense", "运动", "sport"));

        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setType("expense");
        dto.setName("宠物");
        assertThrows(BusinessException.class, () -> categoryService.update(USER_A, second.getId(), dto));
    }

    @Test
    @DisplayName("3.3 修改自定义分类：他人分类不可修改")
    void update_notOwned() {
        CategoryVO created = categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));

        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setType("expense");
        dto.setName("他人宠物");
        assertThrows(BusinessException.class, () -> categoryService.update(USER_B, created.getId(), dto));
    }

    @Test
    @DisplayName("3.3 修改自定义分类：type 不匹配不可修改")
    void update_typeMismatch() {
        CategoryVO created = categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));

        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setType("income");
        dto.setName("宠物");
        assertThrows(BusinessException.class, () -> categoryService.update(USER_A, created.getId(), dto));
    }

    @Test
    @DisplayName("3.4 删除自定义分类：删除后列表移除，他人分类不可删")
    void delete() {
        CategoryVO created = categoryService.add(USER_A, buildDTO("expense", "宠物", "pet"));

        assertTrue(categoryService.delete(USER_A, created.getId(), "expense"));
        assertTrue(categoryService.list(USER_A, "expense").getCustom().isEmpty());
        // 历史账单不受影响（账单保存的是分类名称快照，此处验证删除不牵连账单表）
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_bill", Integer.class));

        // 他人分类不可删除
        CategoryVO other = categoryService.add(USER_B, buildDTO("expense", "宠物", "pet"));
        assertThrows(BusinessException.class, () -> categoryService.delete(USER_A, other.getId(), "expense"));
        // type 不匹配不可删除
        assertThrows(BusinessException.class, () -> categoryService.delete(USER_B, other.getId(), "income"));
    }

    @Test
    @DisplayName("3.4 删除自定义分类：不存在的分类")
    void delete_notExist() {
        assertThrows(BusinessException.class, () -> categoryService.delete(USER_A, "c_not_exist", "expense"));
    }
}

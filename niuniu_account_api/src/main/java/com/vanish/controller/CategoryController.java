package com.vanish.controller;

import com.vanish.common.result.ResultVO;
import com.vanish.service.ICategoryService;
import com.vanish.service.dto.CategoryDTO;
import com.vanish.service.dto.CategoryUpdateDTO;
import com.vanish.service.vo.CategoryListVO;
import com.vanish.service.vo.CategoryVO;
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

/**
 * 分类模块接口
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    /**
     * 3.1 获取分类列表（预设 + 自定义）
     */
    @GetMapping("/list")
    public ResultVO<CategoryListVO> list(@RequestAttribute("userId") String userId,
                                         @RequestParam("type") String type) {
        return ResultVO.successWithData(categoryService.list(userId, type));
    }

    /**
     * 3.2 新增自定义分类
     */
    @PostMapping
    public ResultVO<CategoryVO> add(@RequestAttribute("userId") String userId,
                                    @Valid @RequestBody CategoryDTO dto) {
        return ResultVO.successWithData(categoryService.add(userId, dto));
    }

    /**
     * 3.3 修改自定义分类
     */
    @PutMapping("/{id}")
    public ResultVO<Void> update(@RequestAttribute("userId") String userId,
                                 @PathVariable("id") String id,
                                 @Valid @RequestBody CategoryUpdateDTO dto) {
        categoryService.update(userId, id, dto);
        return ResultVO.successWithMessage("修改成功");
    }

    /**
     * 3.4 删除自定义分类（历史账单不受影响）
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> delete(@RequestAttribute("userId") String userId,
                                 @PathVariable("id") String id,
                                 @RequestParam("type") String type) {
        categoryService.delete(userId, id, type);
        return ResultVO.successWithMessage("删除成功");
    }
}

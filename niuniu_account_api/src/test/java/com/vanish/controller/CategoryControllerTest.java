package com.vanish.controller;

import com.vanish.NiuniuAccountApplication;
import com.vanish.common.util.JwtUtil;
import com.vanish.service.ICategoryService;
import com.vanish.service.dto.CategoryDTO;
import com.vanish.service.dto.CategoryUpdateDTO;
import com.vanish.service.vo.CategoryListVO;
import com.vanish.service.vo.CategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 分类模块接口测试
 */
@SpringBootTest(classes = NiuniuAccountApplication.class)
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ICategoryService categoryService;

    private String authHeader;

    @BeforeEach
    void setUp() {
        authHeader = "Bearer " + jwtUtil.generateToken("u_test");
    }

    @Test
    @DisplayName("3.1 GET /api/category/list：分类列表")
    void list() throws Exception {
        when(categoryService.list(anyString(), eq("expense")))
                .thenReturn(CategoryListVO.builder()
                        .preset(List.of(CategoryVO.builder().name("餐饮").icon("food").build()))
                        .custom(List.of(CategoryVO.builder().id("c_1").name("宠物").icon("pet").build()))
                        .build());

        mockMvc.perform(get("/api/category/list").param("type", "expense")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.preset[0].name").value("餐饮"))
                .andExpect(jsonPath("$.data.custom[0].id").value("c_1"));
    }

    @Test
    @DisplayName("3.2 POST /api/category：新增自定义分类")
    void add() throws Exception {
        when(categoryService.add(anyString(), any(CategoryDTO.class)))
                .thenReturn(CategoryVO.builder().id("c_1689897600_xyz").name("宠物").icon("pet").build());

        mockMvc.perform(post("/api/category")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"expense\",\"name\":\"宠物\",\"icon\":\"pet\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("c_1689897600_xyz"))
                .andExpect(jsonPath("$.data.name").value("宠物"));
    }

    @Test
    @DisplayName("3.2 POST /api/category：类型非法返回参数校验错误")
    void add_invalidType() throws Exception {
        mockMvc.perform(post("/api/category")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"other\",\"name\":\"宠物\",\"icon\":\"pet\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("3.3 PUT /api/category/{id}：修改自定义分类")
    void update() throws Exception {
        when(categoryService.update(anyString(), eq("c_1"), any(CategoryUpdateDTO.class))).thenReturn(true);

        mockMvc.perform(put("/api/category/c_1")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"expense\",\"name\":\"宠物用品\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("修改成功"));
    }

    @Test
    @DisplayName("3.4 DELETE /api/category/{id}：删除自定义分类")
    void deleteCategory() throws Exception {
        when(categoryService.delete(anyString(), eq("c_1"), eq("expense"))).thenReturn(true);

        mockMvc.perform(delete("/api/category/c_1")
                        .param("type", "expense")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }
}

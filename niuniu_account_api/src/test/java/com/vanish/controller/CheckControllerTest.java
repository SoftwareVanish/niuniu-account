package com.vanish.controller;

import com.vanish.NiuniuAccountApplication;
import com.vanish.common.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 健康检查接口测试
 */
@SpringBootTest(classes = NiuniuAccountApplication.class)
@AutoConfigureMockMvc
class CheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("check.do：免鉴权可访问，返回 UP")
    void check() throws Exception {
        mockMvc.perform(get("/check.do"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("UP"));
    }

    @Test
    @DisplayName("未知接口：带 token 请求返回 404 JSON")
    void notFound() throws Exception {
        String authHeader = "Bearer " + jwtUtil.generateToken("u_test");
        mockMvc.perform(get("/no/such/path").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}

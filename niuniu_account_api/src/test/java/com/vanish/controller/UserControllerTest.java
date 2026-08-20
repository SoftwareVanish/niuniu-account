package com.vanish.controller;

import com.vanish.NiuniuAccountApplication;
import com.vanish.common.util.JwtUtil;
import com.vanish.service.IUserService;
import com.vanish.service.dto.UserLoginDTO;
import com.vanish.service.vo.LoginVO;
import com.vanish.service.vo.UserInfoVO;
import com.vanish.service.vo.UserUpdateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户模块接口测试
 */
@SpringBootTest(classes = NiuniuAccountApplication.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private IUserService userService;

    private String authHeader;

    @BeforeEach
    void setUp() {
        authHeader = "Bearer " + jwtUtil.generateToken("u_test");
    }

    @Test
    @DisplayName("1.1 POST /api/user/login：登录成功返回 token")
    void login() throws Exception {
        when(userService.login(any(UserLoginDTO.class)))
                .thenReturn(LoginVO.builder()
                        .token("mock-token")
                        .userId("u_1689a3f2")
                        .nickName("小明")
                        .avatarUrl("https://cdn.example.com/a.png")
                        .build());

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"wx_code\",\"nickName\":\"小明\",\"avatarUrl\":\"https://cdn.example.com/a.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-token"))
                .andExpect(jsonPath("$.data.userId").value("u_1689a3f2"))
                .andExpect(jsonPath("$.data.nickName").value("小明"));
    }

    @Test
    @DisplayName("1.1 POST /api/user/login：昵称为空返回参数校验错误")
    void login_invalidParam() throws Exception {
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"wx_code\",\"nickName\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("昵称不能为空"));
    }

    @Test
    @DisplayName("1.2 GET /api/user/info：无 token 返回 401")
    void info_unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("1.2 GET /api/user/info：带 token 返回用户信息")
    void info() throws Exception {
        when(userService.getUserInfo(anyString()))
                .thenReturn(UserInfoVO.builder()
                        .userId("u_test")
                        .nickName("小明")
                        .avatarUrl("https://cdn.example.com/a.png")
                        .loginTime(1689897600000L)
                        .build());

        mockMvc.perform(get("/api/user/info").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value("u_test"))
                .andExpect(jsonPath("$.data.nickName").value("小明"))
                .andExpect(jsonPath("$.data.loginTime").value(1689897600000L));
    }

    @Test
    @DisplayName("1.2 GET /api/user/info：伪造 token 返回 401")
    void info_badToken() throws Exception {
        mockMvc.perform(get("/api/user/info").header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("1.3 PUT /api/user/info：更新用户信息")
    void updateInfo() throws Exception {
        when(userService.updateUserInfo(anyString(), any()))
                .thenReturn(UserUpdateVO.builder()
                        .nickName("新昵称")
                        .avatarUrl("https://cdn.example.com/b.png")
                        .build());

        mockMvc.perform(put("/api/user/info")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickName\":\"新昵称\",\"avatarUrl\":\"https://cdn.example.com/b.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickName").value("新昵称"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://cdn.example.com/b.png"));
    }

    @Test
    @DisplayName("1.4 POST /api/user/logout：退出登录")
    void logout() throws Exception {
        mockMvc.perform(post("/api/user/logout").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("已退出登录"));
    }
}

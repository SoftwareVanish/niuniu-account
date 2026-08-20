package com.vanish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vanish.common.exception.BusinessException;
import com.vanish.common.util.JwtUtil;
import com.vanish.dao.entity.User;
import com.vanish.dao.mapper.UserMapper;
import com.vanish.service.dto.UserLoginDTO;
import com.vanish.service.dto.UserUpdateDTO;
import com.vanish.service.vo.LoginVO;
import com.vanish.service.vo.UserInfoVO;
import com.vanish.service.wechat.WeChatApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 用户服务单元测试（真实 MySQL 测试库）
 */
@SpringBootTest(classes = ServiceTestApplication.class)
class UserServiceTest {

    private static final String OPENID_1 = "openid_test_1";
    private static final String OPENID_2 = "openid_test_2";

    @Autowired
    private IUserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private WeChatApiClient weChatApiClient;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.update("DELETE FROM t_bill");
        jdbcTemplate.update("DELETE FROM t_custom_category");
        jdbcTemplate.update("DELETE FROM t_user");
    }

    private UserLoginDTO buildLoginDTO(String code, String nickName, String avatarUrl) {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode(code);
        dto.setNickName(nickName);
        dto.setAvatarUrl(avatarUrl);
        return dto;
    }

    @Test
    @DisplayName("1.1 微信登录：新用户自动注册并签发 token")
    void login_newUser() {
        when(weChatApiClient.code2Session(anyString())).thenReturn(OPENID_1);

        LoginVO vo = userService.login(buildLoginDTO("code_1", "小明", "https://cdn.example.com/a.png"));

        assertNotNull(vo.getToken(), "token 不能为空");
        assertTrue(vo.getUserId().startsWith("u_"), "用户 ID 应以 u_ 开头");
        assertEquals("小明", vo.getNickName());
        assertEquals("https://cdn.example.com/a.png", vo.getAvatarUrl());
        // token 可解析回 userId
        assertEquals(vo.getUserId(), jwtUtil.parseToken(vo.getToken()));
        // 数据库已落库
        User saved = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, OPENID_1));
        assertNotNull(saved, "用户应已注册入库");
        assertEquals(vo.getUserId(), saved.getUserId());
        assertNotNull(saved.getLoginTime(), "登录时间应已记录");
    }

    @Test
    @DisplayName("1.1 微信登录：老用户更新昵称头像，复用原 userId")
    void login_existingUser() {
        // 先注册一个用户
        when(weChatApiClient.code2Session(anyString())).thenReturn(OPENID_1);
        LoginVO first = userService.login(buildLoginDTO("code_1", "小明", null));
        // 换昵称再登录
        LoginVO second = userService.login(buildLoginDTO("code_2", "小明二号", "https://cdn.example.com/b.png"));

        assertEquals(first.getUserId(), second.getUserId(), "老用户应复用原 userId");
        assertEquals("小明二号", second.getNickName());
        assertEquals("https://cdn.example.com/b.png", second.getAvatarUrl());
        // 库中只有一条记录且已更新
        assertEquals(1L, userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getOpenid, OPENID_1)));
        User saved = userMapper.selectById(first.getUserId());
        assertEquals("小明二号", saved.getNickName());
    }

    @Test
    @DisplayName("1.1 微信登录：微信接口失败抛出业务异常")
    void login_wechatFail() {
        when(weChatApiClient.code2Session(anyString()))
                .thenThrow(new BusinessException("微信登录失败：invalid code"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.login(buildLoginDTO("bad_code", "小明", null)));
        assertTrue(e.getMessage().contains("微信登录失败"));
        assertEquals(0L, userMapper.selectCount(null), "登录失败不应落库");
    }

    @Test
    @DisplayName("1.2 获取用户信息")
    void getUserInfo() {
        when(weChatApiClient.code2Session(anyString())).thenReturn(OPENID_1);
        LoginVO login = userService.login(buildLoginDTO("code_1", "小明", null));

        UserInfoVO info = userService.getUserInfo(login.getUserId());

        assertEquals(login.getUserId(), info.getUserId());
        assertEquals("小明", info.getNickName());
        assertNotNull(info.getLoginTime());
    }

    @Test
    @DisplayName("1.2 获取用户信息：用户不存在抛业务异常")
    void getUserInfo_notFound() {
        assertThrows(BusinessException.class, () -> userService.getUserInfo("u_not_exist"));
    }

    @Test
    @DisplayName("1.3 更新用户信息：只更新传入字段")
    void updateUserInfo_partial() {
        when(weChatApiClient.code2Session(anyString())).thenReturn(OPENID_1);
        LoginVO login = userService.login(buildLoginDTO("code_1", "小明", "https://cdn.example.com/old.png"));
        String userId = login.getUserId();

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickName("新昵称");
        var result = userService.updateUserInfo(userId, dto);

        assertEquals("新昵称", result.getNickName(), "昵称应更新");
        assertEquals("https://cdn.example.com/old.png", result.getAvatarUrl(), "未传头像应保留原值");
        User saved = userMapper.selectById(userId);
        assertEquals("新昵称", saved.getNickName());
        assertEquals("https://cdn.example.com/old.png", saved.getAvatarUrl());
    }

    @Test
    @DisplayName("1.3 更新用户信息：清空头像")
    void updateUserInfo_clearAvatar() {
        when(weChatApiClient.code2Session(anyString())).thenReturn(OPENID_1);
        LoginVO login = userService.login(buildLoginDTO("code_1", "小明", "https://cdn.example.com/old.png"));

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setAvatarUrl("");
        var result = userService.updateUserInfo(login.getUserId(), dto);

        assertEquals("", result.getAvatarUrl());
        assertEquals("", userMapper.selectById(login.getUserId()).getAvatarUrl());
    }
}

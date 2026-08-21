package com.vanish.service.wechat;

import com.vanish.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * WeChatApiClient 单元测试
 * 核心：验证微信 code2Session 返回 Content-Type: text/plain（内容实为 JSON）时能正常解析
 */
class WeChatApiClientTest {

    private static final String LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private WeChatApiClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new WeChatApiClient(builder);
        ReflectionTestUtils.setField(client, "appid", "test-appid");
        ReflectionTestUtils.setField(client, "secret", "test-secret");
    }

    @Test
    void code2Session_textPlainResponse_shouldParseOpenid() {
        // 重现线上 bug：微信返回 text/plain 的 JSON，默认转换器会抛 UnknownContentTypeException
        server.expect(requestTo(startsWith(LOGIN_URL)))
                .andRespond(withSuccess("{\"openid\":\"o-test-123\",\"session_key\":\"sk\"}", MediaType.TEXT_PLAIN));

        String openid = client.code2Session("test-code");

        assertEquals("o-test-123", openid);
        server.verify();
    }

    @Test
    void code2Session_jsonResponse_shouldParseOpenid() {
        // 兼容标准 application/json 响应
        server.expect(requestTo(startsWith(LOGIN_URL)))
                .andRespond(withSuccess("{\"openid\":\"o-test-456\",\"session_key\":\"sk\"}", MediaType.APPLICATION_JSON));

        String openid = client.code2Session("test-code");

        assertEquals("o-test-456", openid);
        server.verify();
    }

    @Test
    void code2Session_errorCode_shouldThrowWithWeChatMsg() {
        // 微信返回错误码（如 code 无效）时应抛业务异常并带上微信的 errmsg
        server.expect(requestTo(startsWith(LOGIN_URL)))
                .andRespond(withSuccess("{\"errcode\":40029,\"errmsg\":\"invalid code\"}", MediaType.TEXT_PLAIN));

        BusinessException ex = assertThrows(BusinessException.class, () -> client.code2Session("bad-code"));

        assertTrue(ex.getMessage().contains("invalid code"));
        server.verify();
    }

    @Test
    void code2Session_missingOpenid_shouldThrow() {
        // 响应中没有 openid（如 appid/secret 错误导致 errcode=40013）应抛业务异常
        server.expect(requestTo(startsWith(LOGIN_URL)))
                .andRespond(withSuccess("{\"errcode\":40013,\"errmsg\":\"invalid appid\"}", MediaType.TEXT_PLAIN));

        BusinessException ex = assertThrows(BusinessException.class, () -> client.code2Session("test-code"));

        assertTrue(ex.getMessage().contains("invalid appid"));
        server.verify();
    }
}

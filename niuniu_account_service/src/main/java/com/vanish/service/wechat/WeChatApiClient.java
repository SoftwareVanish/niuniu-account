package com.vanish.service.wechat;

import com.vanish.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 微信 API 客户端：code2Session 换取 openid
 */
@Slf4j
@Component
public class WeChatApiClient {

    private static final String JSCODE_2_SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    private final RestClient restClient;

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    /**
     * 构造器：内部创建 RestClient（Spring Boot 4 已无 RestClient.Builder 自动配置，不能构造器注入）
     */
    public WeChatApiClient() {
        this(RestClient.builder());
    }

    /**
     * 测试专用构造器：允许外部传入 builder 以绑定 MockRestServiceServer
     */
    WeChatApiClient(RestClient.Builder builder) {
        // 微信 code2Session 返回的 Content-Type 是 text/plain（内容实为 JSON），
        // 默认 JSON 转换器只认 application/json，这里注册一个两种类型都支持的转换器
        JacksonJsonHttpMessageConverter jsonConverter = new JacksonJsonHttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        this.restClient = builder
                .messageConverters(converters -> converters.add(0, jsonConverter))
                .build();
    }

    /**
     * code2Session：用小程序登录凭证换取 openid
     *
     * @param code wx.login() 获取的临时凭证
     * @return openid
     * @throws BusinessException 微信接口调用失败或返回错误码
     */
    public String code2Session(String code) {
        WxSessionResponse resp;
        try {
            resp = restClient.get()
                    .uri(JSCODE_2_SESSION_URL, appid, secret, code)
                    .retrieve()
                    .body(WxSessionResponse.class);
        } catch (Exception e) {
            log.error("WeChatApiClient.code2Session | fail | error:", e);
            throw new BusinessException("微信登录失败，请稍后重试");
        }
        if (resp == null || resp.openid() == null || resp.openid().isBlank()) {
            String errMsg = resp == null ? "响应为空" : resp.errmsg();
            log.warn("WeChatApiClient.code2Session | fail | errcode:{} | errmsg:{}",
                    resp == null ? null : resp.errcode(), errMsg);
            throw new BusinessException("微信登录失败：" + errMsg);
        }
        return resp.openid();
    }

    /**
     * code2Session 响应体（record 反序列化时未知字段自动忽略）
     */
    record WxSessionResponse(String openid, String sessionKey, Integer errcode, String errmsg) {
    }
}

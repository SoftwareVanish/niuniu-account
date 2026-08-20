package com.vanish.service.wechat;

import com.vanish.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    public WeChatApiClient() {
        this.restClient = RestClient.create();
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

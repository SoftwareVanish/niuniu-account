package com.vanish.controller;

import com.vanish.common.result.ResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务健康检查接口
 */
@RestController
public class CheckController {

    /**
     * 验证服务状态（免鉴权）
     */
    @GetMapping("/check.do")
    public ResultVO<String> check() {
        return ResultVO.successWithData("UP");
    }
}

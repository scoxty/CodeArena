package com.xty.backend.controller.user.account.oauth2;

import com.alibaba.fastjson2.JSONObject;
import com.xty.backend.service.user.account.oauth2.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WebController {
    @Autowired
    private WebService webService;

    @GetMapping("/codearena/api/user/account/qq/web/apply_code")
    public JSONObject applyCode() {
        return webService.applyCode();
    }

    @GetMapping("/codearena/api/user/account/qq/web/receive_code")
    public JSONObject receiveCode(@RequestParam Map<String, String> data) {
        String code = data.get("code");
        String state = data.get("state");
        return webService.receiveCode(code, state);
    }

}

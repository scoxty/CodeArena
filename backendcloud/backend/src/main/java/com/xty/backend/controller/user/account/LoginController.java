package com.xty.backend.controller.user.account;

import com.xty.backend.service.user.account.LoginService;
import com.xty.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/codearena/api/user/account/token")
    public Map<String, String> login(@RequestParam Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        return loginService.login(username, password);
    }

    @PostMapping("/codearena/api/user/account/refresh_token")
    public Map<String, String> refreshToken(@RequestParam Map<String, String> req) {
        String refreshToken = req.get("refresh_token");
        String userId;

        Map<String, String> resp = new HashMap<>();

        try {
            userId = JwtUtil.parseJWT(refreshToken).getSubject();
        } catch (Exception e) {
            resp.put("error_msg", "refresh_token已过期,请重新登录");
            return resp;
        }

        // 重新生成新的 accessToken 和 refreshToken
        String newAccessToken = JwtUtil.createAccessToken(userId);
        String newRefreshToken = JwtUtil.createRefreshToken(userId);

        resp.put("error_msg", "success");
        resp.put("token", newAccessToken);
        resp.put("refresh_token", newRefreshToken);

        return resp;
    }
}

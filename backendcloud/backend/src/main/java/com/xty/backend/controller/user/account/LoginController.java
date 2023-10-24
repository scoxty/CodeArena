package com.xty.backend.controller.user.account;

import com.xty.backend.service.user.account.LoginService;
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

    @PostMapping("/api/user/account/token")
    public Map<String, String> login(@RequestParam Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        return loginService.login(username, password);

    }
}

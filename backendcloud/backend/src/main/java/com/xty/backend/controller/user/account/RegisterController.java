package com.xty.backend.controller.user.account;

import com.xty.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @PostMapping("/user/account/register")
    public Map<String, String> register(@RequestParam Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        String confirmedPassword = req.get("confirmedPassword");
        return registerService.register(username, password, confirmedPassword);
    }
}

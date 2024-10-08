package com.xty.backend.controller.user.account;

import com.xty.backend.service.impl.user.account.InfoServiceImpl;
import com.xty.backend.service.user.account.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoController {

    @Autowired
    private InfoService infoService;

    @GetMapping("/codearena/api/user/account/info")
    public Map<String, String> getInfo() {
        return infoService.getInfo();
    }
}

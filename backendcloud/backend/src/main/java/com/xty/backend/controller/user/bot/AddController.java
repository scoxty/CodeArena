package com.xty.backend.controller.user.bot;

import com.xty.backend.service.user.bot.AddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AddController {

    @Autowired
    private AddService addService;

    @PostMapping("/codearena/api/user/bot/add")
    public Map<String, String> add(@RequestParam Map<String, String> req) {
        return addService.add(req);
    }
}

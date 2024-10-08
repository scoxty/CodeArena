package com.xty.backend.controller.user.bot;

import com.xty.backend.service.user.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UpdateController {

    @Autowired
    private UpdateService updateService;

    @PostMapping("/codearena/api/user/bot/update")
    public Map<String, String> update(@RequestParam Map<String, String> req) {
        return updateService.update(req);
    }
}

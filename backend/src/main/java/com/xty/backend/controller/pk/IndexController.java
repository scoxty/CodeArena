package com.xty.backend.controller.pk;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/pk")
public class IndexController {

    @RequestMapping("/index")
    public String index() {
        return "pk/index.html";
    }

    @RequestMapping("/getbotinfo")
    @ResponseBody
    public Map<String, String> getBotInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "gpt");
        info.put("rating", "2500");
        System.out.println("方法被调用");
        return info;
    }
}

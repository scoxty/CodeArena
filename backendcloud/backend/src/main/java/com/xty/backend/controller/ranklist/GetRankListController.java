package com.xty.backend.controller.ranklist;

import com.alibaba.fastjson2.JSONObject;
import com.xty.backend.service.ranklist.GetRankListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GetRankListController {
    @Autowired
    private GetRankListService getRankListService;

    @GetMapping("/rankList/getList")
    public JSONObject getList(@RequestParam Map<String, String> req) {
        Integer page = Integer.parseInt(req.get("page"));
        return getRankListService.getList(page);
    }
}

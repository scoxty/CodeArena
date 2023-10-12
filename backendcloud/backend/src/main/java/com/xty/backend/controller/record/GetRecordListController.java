package com.xty.backend.controller.record;

import com.alibaba.fastjson2.JSONObject;
import com.xty.backend.service.record.GetRecordListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GetRecordListController {
    @Autowired
    private GetRecordListService getRecordListService;

    @GetMapping("/record/getList")
    public JSONObject getList(@RequestParam Map<String, String> req) {
        Integer page = Integer.parseInt(req.get("page"));
        return getRecordListService.getList(page);
    }

}

package com.xty.backend.controller.introduction;

import com.xty.backend.service.introduction.GetIntroductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetIntroductionController {
    @Autowired
    private GetIntroductionService getIntroductionService;

    @GetMapping("/api/introduction")
    public String getIntroduction() {
        return getIntroductionService.getIntroduction();
    }
}

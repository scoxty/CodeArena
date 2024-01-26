package com.xty.backend.service.impl.OAuth2;

import com.alibaba.fastjson2.JSONObject;
import com.xty.backend.service.user.account.OAuth2.WebService;
import org.springframework.stereotype.Service;

@Service
public class WebServiceImpl implements WebService {
    @Override
    public JSONObject applyCode() {
        return null;
    }

    @Override
    public JSONObject receiveCode(String code, String state) {
        return null;
    }
}

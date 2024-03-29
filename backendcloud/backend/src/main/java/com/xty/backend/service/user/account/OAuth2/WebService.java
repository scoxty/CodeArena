package com.xty.backend.service.user.account.oauth2;

import com.alibaba.fastjson2.JSONObject;

public interface WebService {
    JSONObject applyCode();
    JSONObject receiveCode(String code, String state);
}

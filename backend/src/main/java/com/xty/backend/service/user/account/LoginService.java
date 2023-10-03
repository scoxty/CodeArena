package com.xty.backend.service.user.account;

import java.util.Map;

public interface LoginService {
    Map<String, String> login(String username, String password);
}

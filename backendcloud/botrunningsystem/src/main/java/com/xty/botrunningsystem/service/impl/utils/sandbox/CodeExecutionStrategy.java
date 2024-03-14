package com.xty.botrunningsystem.service.impl.utils.sandbox;

public interface CodeExecutionStrategy {
    Integer executeCode(Integer userId, String code, String input);
}

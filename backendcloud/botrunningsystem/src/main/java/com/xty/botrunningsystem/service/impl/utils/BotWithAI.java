package com.xty.botrunningsystem.service.impl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotWithAI {
    Integer userId;
    String botCode;
    String input;
    Integer aiId;
    String aiBotCode;
    String input2;
}

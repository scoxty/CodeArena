package com.xty.botrunningsystem.service.impl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bot {
   private Integer userId;
   private String type;
   private String botCode;
   private String input;
   private Integer aiId;
   private String type2;
   private String aiBotCode;
   private String input2;
}

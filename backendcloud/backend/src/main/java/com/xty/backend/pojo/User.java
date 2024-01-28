package com.xty.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // getter setter toString equals
@NoArgsConstructor // 无参构造函数
@AllArgsConstructor // 有参构造函数
public class User {
    private Integer id;
    private String openid;
    private String username;
    private String password;
    private String photo;
    private Integer rating;
}

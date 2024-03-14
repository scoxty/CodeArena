package com.xty.backend;

import com.alibaba.fastjson2.JSON;
import com.xty.backend.websocket.utils.GameDTO;

public class TestFastjson {
    public static void main(String[] args) {
        Dog dog = new Dog("Tom2");
        Cat cat = new Cat("Tom", 10, dog);
        String s = JSON.toJSONString(cat);
        System.out.println(s);
        dog = null;

        Cat cat1 = JSON.parseObject(s, Cat.class);
        System.out.println(cat == cat1);
        System.out.println(cat1.name);

    }
}

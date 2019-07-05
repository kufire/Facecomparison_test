package com.ropeok;

import org.junit.jupiter.api.Test;

class ApiTest {

    Api api = new Api();

    @Test
    void getApiFromSwagger() {

        api.getApiFromSwagger("/api/api-auth.json","/clients",null);
    }
/*
    @Test
    void getRedis() {
        //连接本地的 Redis 服务
        Jedis jedis = new Jedis("localhost");
        System.out.println("连接成功");
        //设置 redis 字符串数据
        jedis.set("runoobkey", "www.runoob.com");
        System.out.println(jedis.get("runoobkey"));
    }*/

}
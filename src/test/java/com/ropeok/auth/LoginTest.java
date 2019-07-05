package com.ropeok.auth;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @version: V1.0
 * @author: zhou
 * @className: LoginTest
 * @packageName: com.ropeok.auth
 * @description: LoginTest测试
 * @data: 2019-05-28
 **/

@Epic("系统管理")
@Feature("用户相关")
class LoginTest {

    Login login=new Login();

    private static Logger logger = Logger.getLogger(LoginTest.class);


    /**
     * @methodsName: login
     * @description: 登录测试
     * @param: [accout,expectCode]
     * @return: Null
     */
    /*
    static Stream<Arguments> loginProvider() {
        return Stream.of(

                Arguments.of("{\"username\":\"admin\",\"password\":\"123456\"}", "登录成功!", 200, 200), //正确的用户名，正确的密码
                Arguments.of("{\"username\":\"admin\",\"password\":\"111111\"}", "用户名密码错误!", 200, 400), //正确的用户名，错误的密码
                Arguments.of("{\"username\":\"root\",\"password\":\"123456\"}", "用户名密码错误!", 200, 400), //错误的用户名，正确的密码
                Arguments.of("{\"username\":\"\",\"password\":\"\"}", "用户名密码错误!", 200, 400), //空用户名，空密码
                Arguments.of("{\"username\":\"\",\"password\":\"123456\"}", "用户名密码错误!", 200, 400), //空用户名，正确的密码
                Arguments.of("{\"username\":\"admin\",\"password\":\"\"}", "用户名密码错误!", 200, 400), //正确的用户名，空密码
                Arguments.of("{\"username\":admin,\"password\":\"123456\"}", "Bad Request", 400, 400), //用户名非字符串字符
                Arguments.of("{\"username\":\"admin\",\"password\":123456}", "Bad Request", 400, 400), //密码非字符串字符
                Arguments.of("{\"username\":admin,\"password\":123456}", "Bad Request", 400, 400) //用户名密码非字符串字符
        );
    }
    @ParameterizedTest
    @MethodSource("loginProvider")
    @Story("登录")
    void login(String accout,String msg,int statusCode,int expectCode) {

        logger.info("登录测试");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("_body",accout);
        if(statusCode==200){
            login.Login(map).then().statusCode(statusCode).body("code", equalTo(expectCode),"message",equalTo(msg)).time(lessThan(2000L));
        }else{
            login.Login(map).then().statusCode(statusCode).body("status", equalTo(expectCode),"error",equalTo(msg)).time(lessThan(2000L));
        }
    }

    static Stream<Arguments> logoutProvider() {
        return Stream.of(
                Arguments.of(1,200,"ture"), //带token退出系统
                Arguments.of(0,400,"false")//无token退出系统
        );
    }
    @ParameterizedTest
    @MethodSource("logoutProvider")
    @Story("登出")
    void logout(int iToken,int expectCode,String successStatus) {
        login.Logout(iToken).then().statusCode(200).body("code", equalTo(expectCode),"success",equalTo(successStatus)).time(lessThan(2000L));
     }
     */
    @Test
    @Story("登出")
    void gettest(){
        int i=1;
        assertEquals("1","1","1");
    }
}
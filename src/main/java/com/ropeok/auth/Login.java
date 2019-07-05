package com.ropeok.auth;

import com.ropeok.App;
import io.restassured.response.Response;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * @version: V1.0
 * @author: zhou
 * @className: User
 * @packageName: com.ropeok
 * @description: Login : 系统管理-用户 的接口
 * @data: 2019-05-28
 **/

public class Login extends App {

    private static Logger logger = Logger.getLogger(Login.class);
    /**
     * @methodsName: userLogin
     * @description: 登录
     * @param: [map]
     * @return: Response
     */
    public Response Login(HashMap<String, Object> map) {
        //Yaml文件读取,pattern部分模糊匹配
        //return getResponseFromYaml("/api/userLogin.yaml",map,0);
        //HAR文件读取,pattern部分模糊匹配
        //return getResponseFromHar("/api/userLogin.har.json", ".*/web/SysUser/userLogin.*",map,0);
        //Swagger文件读取,pattern部分精确匹配
        return getResponseFromSwagger("/api/api-auth.json","/web/login",map,0 );
    }

    /**
     * @methodsName: userLogin
     * @description: 登出
     * @param: [map]
     * @return: Response
     */
    public Response Logout(Integer iToken) {
        HashMap<String, Object> map=new HashMap<>();
        //HAR文件读取,pattern部分模糊匹配
        //return getResponseFromHar("/api/userOut.har.json", ".*/web/SysUser/userOut.*",map,iToken);
        //Swagger文件读取,pattern部分精确匹配
        return getResponseFromSwagger("/api/api-auth.json", "/web/logout",map,iToken);

    }
}

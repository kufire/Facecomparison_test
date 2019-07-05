package com.ropeok;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * @version: V1.0
 * @author: zhou
 * @className: Vedioserver
 * @packageName: com.ropeok.vedioserver
 * @description: 获取token
 * @data: 2019-05-27
 **/

public class Facecomparison {

    private static String token;
    /**
     * @methodsName: getVedioserverToken
     * @description: 获取token
     * @param: [usr，pwd]
     * @return: String
     */
    public static String getRestrictedAreaToken(String usr,String pwd){
        return "bearerc"+RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"123456\"}")
                .when().post("http://192.168.22.105:4000/api-auth/web/login")
                .then().log().all().statusCode(200)
                .extract().path("result.access_token");   //  data.token:json中token定位的位置
    }

    /**
     * @methodsName: getToken
     * @description: 读取配置文件，获取token
     * @param: Null
     * @return: String
     */
    public static String getToken(){
        //获取token
        if(token==null){
            token=getRestrictedAreaToken(FacecomparisonConfig.getInstance().account, FacecomparisonConfig.getInstance().password);
        }
        return token;
    }
}
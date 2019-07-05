package com.ropeok;


import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * @version: V1.0
 * @author: zhou
 * @className: App
 * @packageName: com.ropeok
 * @description: 重写getDefaultRequestSpecification，执行given带token（token放入header中）
 * @data: 2019-05-27
 **/

public class App extends Api {
    //String random=String.valueOf(System.currentTimeMillis());

    /**
     * @methodsName: getDefaultRequestSpecification
     * @description: 重写getDefaultRequestSpecification
     * @param: Null
     * @return: String
     */
    @Override
    public RequestSpecification getDefaultRequestSpecification() {

        RequestSpecification requestSpecification=super.getDefaultRequestSpecification();
        requestSpecification.contentType(ContentType.JSON);//项目都使用Accept: application/json
        requestSpecification.header("Authorization", Facecomparison.getToken());  //token放入header中

        requestSpecification.filter( (req, res, ctx)->{
            //todo: 对请求 响应做封装(无加密，可以直接返回)
            return ctx.next(req, res);
        });

        return requestSpecification;
    }

}

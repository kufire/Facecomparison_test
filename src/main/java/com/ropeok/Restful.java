package com.ropeok;

import java.util.HashMap;

/**
 * @version: V1.0
 * @author: zhou
 * @className: Restful
 * @packageName:
 * @description: rest接口模板
 * @data: 2019-05-27
 **/
public class Restful {

    /** url地址 **/
    public String url;
    /** 方法 **/
    public String method;
    /** head **/
    public HashMap<String, String> headers=new HashMap<String, String>();
    /** query **/
    public HashMap<String, String> query=new HashMap<String, String>();
    /** body **/
    public String body;
}

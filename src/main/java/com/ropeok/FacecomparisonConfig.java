package com.ropeok;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.HashMap;


/**
 * @version: V1.0
 * @author: zhou
 * @className: RestrictedAreaConfig
 * @packageName: com.ropeok.vedioserver
 * @description: 获取配置文件参数
 * @data: 2019-05-27
 **/

public class FacecomparisonConfig {


    public String account="admin";
    public String password="123456";
    public String current;
    public HashMap<String, HashMap<String, String>> env;

    public String sql_user;
    public String sql_password;
    public String sql_driver;
    public String sql_url;


    private static FacecomparisonConfig facecomparisonConfig;

    /**
     * @methodsName: getInstance
     * @description: 获取配置文件数据
     * @param: Null
     * @return: VedioserverConfig
     */
    public static FacecomparisonConfig getInstance(){

        if(facecomparisonConfig==null){
            facecomparisonConfig=load("/conf/FacecomparisonConfig.yaml");
        }
        return facecomparisonConfig;
    }


    /**
     * @methodsName: load
     * @description: 获取YAML文件数据
     * @param: [path]
     * @return: FacecomparisonConfig
     */
    public static FacecomparisonConfig load(String path){
        //read from yaml
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(FacecomparisonConfig.class.getResourceAsStream(path), FacecomparisonConfig.class);
            //System.out.println(mapper.writeValueAsString(VedioConfig.getInstance()));

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
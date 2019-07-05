package com.ropeok;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.useRelaxedHTTPSValidation;

/**
 * @version: V1.0
 * @author: zhou
 * @className: Api
 * @packageName: com.ropeok
 * @description: rest接口常用函数封装
 * @data: 2019-05-27
 **/

public class Api {

    HashMap<String, Object> query = new HashMap<String, Object>();
    private static Logger logger = Logger.getLogger(Api.class);
    /** Https **/
    public Api(){
        useRelaxedHTTPSValidation();
    }

    /** 获取正确token的RequestSpecification （app中重写该类）**/
    public RequestSpecification getDefaultRequestSpecification() {
        return given().log().all();
    }

    /** 获取无token的RequestSpecification **/
    public RequestSpecification getNoneTokenRequestSpecification() {
        return given().contentType(ContentType.JSON).log().all();
    }

    /** 获取错误token的RequestSpecification **/
    public RequestSpecification getErrorTokenRequestSpecification() {
        return given().header("token", "error token").contentType(ContentType.JSON).log().all();
    }
    /**
     * @methodsName: template
     * @description: 将JSON文件数据，转为String
     * @param: [path,map]
     * @return: String
     */
    public static String template(String path, HashMap<String, Object> map) {
        //read from json
        DocumentContext documentContext = JsonPath.parse(Api.class
                .getResourceAsStream(path));
        map.entrySet().forEach(entry -> {
            documentContext.set(entry.getKey(), entry.getValue());
        });
        return documentContext.jsonString();
    }

    /**
     * @methodsName: getApiFromSwagger
     * @description: 从json格式的Swagger文件中，获取对应url的API参数值
     * @param: [path,pattern,method]
     * @return: Restful
     */
    public Restful getApiFromSwagger(String path, String pattern,String method) {

        Map<String, String> map = new HashMap<String, String>();
        Restful restful = new Restful();
        String content=JsonPath.parse(Api.class.getResourceAsStream(path)).jsonString();
        //解析数据
        JSONObject jsonObject = new JSONObject(content);
        //url为 http协议
        restful.url="http://"+jsonObject.getString("host")+jsonObject.getString("basePath")+pattern;
        //找到匹配的url，精确查询
        if(true==jsonObject.getJSONObject("paths").has(pattern)){

            JSONArray array_method = jsonObject.getJSONObject("paths").getJSONObject(pattern).names();

            //一个接口有多个方法
            for (int n = 0; n < array_method.length(); n++) {

                restful.method = jsonObject.getJSONObject("paths").getJSONObject(pattern).names().get(n).toString();
                JSONObject methodObject = jsonObject.getJSONObject("paths").getJSONObject(pattern).getJSONObject(restful.method);
                //看是否包含 key：parameters
                if(methodObject.has("parameters")){
                    JSONArray array = methodObject.getJSONArray("parameters");

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        /**
                         *获取query参数,值设置为空
                         */
                        if (o.getString("in").toString().equals("query")) {
                            restful.query.put(o.getString("name"), "");
                        }
                        /**
                         *body部分不做参数设置，由测试用例中参数化直接赋值
                         */
                        restful.body = "";

                /*
                if(o.getString("in").toString().equals("body")){
                    String schema = o.getJSONObject("schema").getString("$ref").substring(("#/definitions/").length());
                    JSONObject jsonObjectbody =jsonObject.getJSONObject("definitions").getJSONObject(schema).getJSONObject("properties");
                    for(String str:jsonObjectbody.keySet()){
                        map.put(str,"");
                    }
                    restful.body=map.toString();
                    logger.info(restful.body);
                }*/
                    }
                    if(restful.method==method){
                        break;               //swagger中的方法和要找的方法一致，就退出查找
                    }
                }else{
                    restful.body = "";
                }

            }

        }
        return restful;
    }

    /**
     * @methodsName: getApiFromHar
     * @description: 从HAR文件中，获取对应url的API参数值
     * @param: [path,pattern]
     * @return: Restful
     */
    public Restful getApiFromHar(String path, String pattern) {
        HarReader harReader = new HarReader();
        try {
            //读取HAR文件
            Har har = harReader.readFromFile(new File(
                    URLDecoder.decode(
                            getClass().getResource(path).getPath(), "utf-8"
                    )));

            /**
             * 查找HAR文件中和pattern匹配的部分
             */
            HarRequest request = new HarRequest();
            Boolean match=false;
            for (HarEntry entry : har.getLog().getEntries()) {
                request = entry.getRequest();
                if (request.getUrl().matches(pattern)) {
                    match=true;
                    break;
                }
            }
            if(match==false){
                request=null;
                throw new Exception("not found pattern");
            }

            /**
             * 获取匹配部分的method
             */
            Restful restful = new Restful();
            restful.method = request.getMethod().name().toLowerCase();

            /**
             * 获取匹配部分的url，去掉url中的query部分
             */
            //restful.url = request.getUrl();
            Pattern patternurl = Pattern.compile("(.*)\\?.*");//url中 ？ 后面是query该部分去除
            Matcher matcher = patternurl.matcher(request.getUrl());
            if (matcher.find()) {
                restful.url = matcher.group(1);
            }else{
                restful.url = request.getUrl();
            }
            /**
             * 获取匹配部分的query
             */
            request.getQueryString().forEach(q -> {
                restful.query.put(q.getName(), q.getValue());
            });
            /**
             * 获取匹配部分的body
             */
            restful.body = request.getPostData().getText();

            return restful;

        } catch (HarReaderException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @methodsName: getApiFromYaml
     * @description: 从Yaml文件中，获取对应url的API参数值
     * @param: [path]
     * @return: Restful
     */
    public Restful getApiFromYaml(String path) {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(FacecomparisonConfig.class.getResourceAsStream(path), Restful.class);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @methodsName: updateApiFromMap
     * @description: 更新Restful参数的数据
     * @param: [restful,map]
     * @return: Restful
     */
    public Restful updateApiFromMap(Restful restful, HashMap<String, Object> map) {

        if(map==null){
            return  restful;
        }

        /**
         * 更新get的query参数传入数据
         */
        if (restful.method.toLowerCase().contains("get")) {
            map.entrySet().forEach(entry -> {
                restful.query.replace(entry.getKey(), entry.getValue().toString());
            });
        }
        /**
         * 更新post的传入数据
         */
        if (restful.method.toLowerCase().contains("post")) {
            if (map.containsKey("_body")) {
                //body传入数据
                restful.body = map.get("_body").toString();
            }else if (map.containsKey("_file")) {
                //file传入数据
                String filePath = map.get("_file").toString();
                map.remove("_file");
                restful.body = template(filePath, map);
            }else{
                //query参数
                map.entrySet().forEach(entry -> {
                    restful.query.replace(entry.getKey(), entry.getValue().toString());
                });
            }

        }
        return restful;

    }

    /**
     * @methodsName: getResponseFromRestful
     * @description: 获取Response数据
     * @param: [restful,bToken]
     * @return: Response
     */
    public Response getResponseFromRestful(Restful restful,Integer iToken) {

        RequestSpecification requestSpecification;
        /**
         * header是否带token
         */
        if (1==iToken) {
             requestSpecification = getDefaultRequestSpecification();
            //System.out.println(requestSpecification.header(""));
        }else if(1==iToken){
            requestSpecification = getNoneTokenRequestSpecification();
        }else{
            requestSpecification = getErrorTokenRequestSpecification();
        }
        /**
         * query请求数据
         */
        if (restful.query != null) {
            restful.query.entrySet().forEach(entry -> {
                requestSpecification.queryParam(entry.getKey(), entry.getValue());
            });
        }
        /**
         * body请求数据
         */
        if (restful.body != null) {
            requestSpecification.body(restful.body);
        }

        //多环境时，更新url的host值
        String[] url=updateUrl(restful.url);
        return requestSpecification.log().all()
                .when().request(restful.method, url[1])
                .then().log().all()
                .extract().response();
    }

    /**
     * @methodsName: getResponseFromYaml
     * @description: 根据yaml生成接口定义并发送
     * @param: [path,map,iToken]
     * @return: Response
     */
    public Response getResponseFromYaml(String path, HashMap<String, Object> map, Integer iToken) {
        //fixed: 根据yaml生成接口定义并发送
        Restful restful = getApiFromYaml(path);
        restful = updateApiFromMap(restful, map);
        return getResponseFromRestful(restful,iToken);

    }

    /**
     * @methodsName: getResponseFromHar
     * @description: 根据HAR生成接口定义并发送
     * @param: [path,pattern, map, iToken]
     * @return: Response
     */
    public Response getResponseFromHar(String path, String pattern, HashMap<String, Object> map, Integer iToken) {
        Restful restful=getApiFromHar(path, pattern);
        restful=updateApiFromMap(restful, map);
        return getResponseFromRestful(restful,iToken);
    }

    /**
     * @methodsName: 重载getResponseFromSwagger
     * @description: 根据JSON格式的Swagger生成接口定义并发送
     * @param: [path,pattern, map, iToken,method]
     * @return: Response
     */
    public Response getResponseFromSwagger(String path, String pattern, HashMap<String, Object> map, Integer iToken) {
        String method = null;
        return getResponseFromSwagger(path, pattern, map, iToken, method);
    }
    /**
     * @methodsName: getResponseFromSwagger
     * @description: 根据JSON格式的Swagger生成接口定义并发送
     * @param: [path,pattern, map, iToken,method]
     * @return: Response
     */
    public Response getResponseFromSwagger(String path, String pattern, HashMap<String, Object> map, Integer iToken,String method) {
        Restful restful=getApiFromSwagger(path, pattern,method);
        restful=updateApiFromMap(restful, map);
        return getResponseFromRestful(restful,iToken);
    }

    /**
     * @methodsName: public String[] updateUrl(String url) {
     * @description: 多环境支持，替换url，更新host的header
     * @param: [url]
     * @return: String[]
     */
    public String[] updateUrl(String url) {

        HashMap<String, String> hosts=FacecomparisonConfig.getInstance().env.get(FacecomparisonConfig.getInstance().current);

        String host="";
        String urlNew="";
        for(Map.Entry<String, String> entry : hosts.entrySet()){
            if(url.contains(entry.getKey())){
                host=entry.getKey();
                urlNew=url.replace(entry.getKey(), entry.getValue());
            }
        }

        return new String[]{host, urlNew};

    }

    /**
     * @methodsName: public String[] getSql(String sql) {
     * @description: 连接数据库，并执行指令
     * @param: [sql]
     * @return: String[]
     */
    public Boolean getSql(String sql) {

        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            //Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName(FacecomparisonConfig.getInstance().sql_driver);
            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(FacecomparisonConfig.getInstance().sql_url, FacecomparisonConfig.getInstance().sql_user, FacecomparisonConfig.getInstance().sql_password);
            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            if(sql.contains("select")){
                ResultSet rs = stmt.executeQuery(sql);
                // 数据库中找到结果
                if (rs.next()) {
                    return true;
                }
                // 完成后关闭
                rs.close();
            }else{
                stmt.executeUpdate(sql);
            }
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return false;
    }

    /**
     * @methodsName: public String[] getRedis(String sql) {
     * @description: 连接数据库，并执行指令
     * @param: [sql]
     * @return: String[]
     */
    public String getRedis(String redis) {

        //连接本地的 Redis 服务
        Jedis jedis = new Jedis("localhost");
        System.out.println("连接成功");
        //设置 redis 字符串数据
        //jedis.set("runoobkey", "www.runoob.com");
        return jedis.get(redis);
        //return false;
    }

    //todo: 支持wsdl soap

    public static String templateFromMustache(String path, HashMap<String, Object> map) {
        //new DefaultMustacheFactory().compile("/data/create.mustache").execute()
        //todo：
        return null;
        //return documentContext.jsonString();
    }
    public Response readApiFromYaml(String path, HashMap<String, Object> map) {
        //todo: 动态调用
        return null;
    }

    /**
     * @methodsName: public void waitfor(Integer time) {
     * @description: 等待
     * @param: [time]
     * @return: null
     */
    public void waitfor(Integer time) {
        try
        {
            Thread.currentThread().sleep(time);//毫秒
        }
        catch(Exception e){}
    }

}

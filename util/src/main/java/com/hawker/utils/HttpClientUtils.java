package com.hawker.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Http访问工具类。
 */
public class HttpClientUtils {
    /**
     * 字符编码
     */
    public static final String CHARACTER_ENCODING = "UTF-8";
    /**
     * ="/"
     */
    public static final String PATH_SIGN = "/";
    /**
     * 标示post方法，="POST".
     */
    public static final String METHOD_POST = "POST";
    /**
     * 标示get方法，="GET".
     */
    public static final String METHOD_GET = "GET";
    /**
     * 标示DELETE方法，="DELETE".
     */
    public static final String METHOD_DELETE = "DELETE";
    /**
     * 标示PUT方法，="PUT".
     */
    public static final String METHOD_PUT = "PUT";
    /**
     * ="Content-Type"
     */
    public static final String CONTENT_TYPE = "Content-Type";

    static {
        System.setProperty("sun.net.client.defaultConnectTimeout", "50000");
        System.setProperty("sun.net.client.defaultReadTimeout", "50000");
    }

    private static String inputStreamToString(InputStream is, String encode) throws Exception {
        StringBuffer buffer = new StringBuffer();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, encode));
        int ch;
        for (int length = 0; (ch = rd.read()) > -1; length++) {
            buffer.append((char) ch);
        }
        rd.close();
        return buffer.toString();
    }

    /**
     * 发送get请求，获取返回html
     *
     * @param strUrl 请求地址
     * @param encode 页面编码
     * @return String
     * @throws Exception
     */
    public static String sendGetRequest(String strUrl, String encode) throws Exception {
        URL newUrl = new URL(strUrl);
        HttpURLConnection hConnect = (HttpURLConnection) newUrl.openConnection();
        InputStream is = hConnect.getInputStream();
        String str = inputStreamToString(is, encode);
        is.close();
        hConnect.disconnect();
        return str;
    }


    /**
     * 发送delete请求，获取返回html
     *
     * @param requestUrl 请求地址
     * @param encode     页面编码
     * @return String
     * @throws Exception
     */
    public static String sendDeleteRequest(String requestUrl, String encode) throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(METHOD_DELETE);// 提交模式
        conn.setConnectTimeout(200000);// 连接超时 单位毫秒
        conn.setReadTimeout(200000);// 读取超时 单位毫秒
        conn.setDoInput(true);
        conn.setUseCaches(false);

        InputStream is = conn.getInputStream();
        String str = inputStreamToString(is, encode);
        is.close();
        conn.disconnect();
        return str;
    }

    /**
     * 发送post请求
     *
     * @param requestUrl 请求URL地址
     * @param params     请求参数 text1=aaa&text2=bbb
     * @param encode     请求参数及页面的编码
     * @return String 返回页面返回的html
     * @throws Exception
     */
    public static String sendPostRequest(String requestUrl, String params, String encode) throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("ContentType", "text/xml;charset=utf-8");
        conn.setRequestMethod(METHOD_POST);// 提交模式
        conn.setConnectTimeout(200000);// 连接超时 单位毫秒
        conn.setReadTimeout(200000);// 读取超时 单位毫秒
        conn.setDoOutput(true);// 是否输入参数
        conn.setDoInput(true);
        conn.setUseCaches(false);

        byte[] b = params.toString().getBytes(encode);
        OutputStream os = conn.getOutputStream();
        os.write(b);// 输入参数
        os.flush();
        os.close();

        InputStream is = conn.getInputStream();
        String str = inputStreamToString(is, encode);
        is.close();
        conn.disconnect();

        return str;
    }

    /**
     * 使用Apache的HttpClient
     *
     * @param requestUrl
     * @param params
     * @param encode
     * @return
     * @throws Exception
     */
    public static String sendPostRequest(String requestUrl, Map<String, String> params, String encode) throws Exception {
        HttpClient httpClient = new HttpClient();// 创建一个客户端，类似打开一个浏览器
        PostMethod postMethod = new PostMethod(requestUrl);
        try {
            postMethod.addRequestHeader("Connection", "close");
            postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 200000); // 设置
            // post
            // 方法请求超时为
            // 10
            // 秒
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(200000); // 设置
            // http
            // 连接超时为
            // 10
            // 秒

            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            if (params != null && !params.isEmpty()) {
                for (String key : params.keySet())
                    pairList.add(new NameValuePair(key, params.get(key)));
            }
            postMethod.addParameters((NameValuePair[]) pairList.toArray(new NameValuePair[pairList.size()]));
            httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
            int statusCode = httpClient.executeMethod(postMethod);
            InputStream is = postMethod.getResponseBodyAsStream();
            String str = inputStreamToString(is, encode);
            is.close();
            return str;
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * 发送put请求
     *
     * @param requestUrl 请求URL地址
     * @param params     请求参数 text1=aaa&text2=bbb
     * @param encode     请求参数及页面的编码
     * @return 返回页面返回的html
     * @throws Exception
     */
    public static String sendPutRequest(String requestUrl, String params, String encode) throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(METHOD_PUT);// 提交模式
        conn.setConnectTimeout(200000);// 连接超时 单位毫秒
        conn.setReadTimeout(200000);// 读取超时 单位毫秒
        conn.setDoOutput(true);// 是否输入参数
        conn.setDoInput(true);
        conn.setUseCaches(false);

        byte[] b = params.toString().getBytes(encode);
        OutputStream os = conn.getOutputStream();
        os.write(b);// 输入参数
        os.flush();
        os.close();

        InputStream is = conn.getInputStream();
        String str = inputStreamToString(is, encode);
        is.close();
        conn.disconnect();
        return str;
    }


    /**
     * url解码
     *
     * @param url
     * @return String 解码后的字符串,当异常时返回原始字符串。
     */
    public static String decode(String url) {
        if (url == null) {
            return null;
        }
        try {
            return URLDecoder.decode(url, CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException ex) {
            return url;
        }
    }

    /**
     * url编码
     *
     * @param url
     * @return String 编码后的字符串,当异常时返回原始字符串。
     */
    public static String encode(String url) {
        if (url == null) {
            return null;
        }
        try {
            return URLEncoder.encode(url, CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException ex) {
            return url;
        }
    }

    public static String postFile(File file, String fileName, String url) throws Exception {
        FileBody bin = null;
        org.apache.http.client.HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        if (file != null) {
            bin = new FileBody(file);
        }
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("upload", bin);
        reqEntity.addPart("suffix", new StringBody(fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())));
        httppost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            InputStream in = resEntity.getContent();
            String str = inputStreamToString(in, CHARACTER_ENCODING);
            return str;
        }

        return null;

    }


    // =========================================================================================== //


    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);


    /**
     * jsonPost
     *
     * @param url    路径
     * @param params 参数
     * @return
     */
    public static JSONObject jsonPost(String url, Map<String, String> params) {
        Map<String, Object> dataMap = new HashMap<>();
        for (String key : params.keySet()) {
            dataMap.put(key, params.get(key));
        }

        return jsonPost(url, new JSONObject(dataMap), false);
    }

    /**
     * jsonPost
     *
     * @param url    路径
     * @param params 参数
     * @return JSONArray
     */
    public static JSONArray jsonPostArray(String url, Map<String, String> params) {
        Map<String, Object> dataMap = new HashMap<>();
        for (String key : params.keySet()) {
            dataMap.put(key, params.get(key));
        }

        return jsonPostArray(url, new JSONObject(dataMap), false);
    }

    /**
     * post请求
     *
     * @param url            url地址
     * @param jsonParam      参数
     * @param noNeedResponse 不需要返回结果
     * @return
     */
    public static JSONArray jsonPostArray(String url, JSONObject jsonParam, boolean noNeedResponse) {
        //post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost method = new HttpPost(url);
        try {
            if (null != jsonParam) {
                //解决中文乱码问题
                StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            return buildResultArray(url, result, noNeedResponse);
        } catch (IOException e) {
            logger.error("通信异常:" + url, e);
        }
        return null;
    }

    private static JSONArray buildResultArray(String url, HttpResponse result, boolean noNeedResponse) {
        int statusCode = result.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            logger.error("通信异常:code=" + statusCode + "," + url + "," + result.getStatusLine());
            throw new RuntimeException("通信异常");
        }

        /**读取服务器返回过来的json字符串数据**/
        String str;
        try {
            str = EntityUtils.toString(result.getEntity());
        } catch (IOException e) {
            logger.error("获取返回数据异常", e);
            throw new RuntimeException(e);
        }

        if (noNeedResponse) {
            return null;
        }

        /**把json字符串转换成json对象**/
        try {
            return JSONArray.parseArray(str);
        } catch (Exception e) {
            logger.error("JSON转换异常,无法将字符串转换成JSON对象\n" + str, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * jsonPost
     *
     * @param url    路径
     * @param params 参数
     * @return
     */
    public static JSONObject httpPost(String url, Map<String, String> params) {
        return httpPost(url, params, false);
    }


    /**
     * post请求
     *
     * @param url            url地址
     * @param jsonParam      参数
     * @param noNeedResponse 不需要返回结果
     * @return
     */
    public static JSONObject jsonPost(String url, JSONObject jsonParam, boolean noNeedResponse) {
        //post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost method = new HttpPost(url);
        try {
            if (null != jsonParam) {
                //解决中文乱码问题
                StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");
            /**请求发送成功，并得到响应**/
            return buildResult(url, result, noNeedResponse);
        } catch (IOException e) {
            logger.error("通信异常:" + url, e);
        }
        return null;
    }


    /**
     * post请求
     *
     * @param url            url地址
     * @param postMap        参数
     * @param noNeedResponse 不需要返回结果
     * @return
     */
    public static JSONObject httpPost(String url, Map<String, String> postMap, boolean noNeedResponse) {
        //post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost method = new HttpPost(url);
        try {
            if (null != postMap) {
                //解决中文乱码问题
                List<org.apache.http.NameValuePair> params = new ArrayList<>();
                for (String key : postMap.keySet()) {
                    params.add(new BasicNameValuePair(key, postMap.get(key)));
                }
                method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            }
            HttpResponse result = httpClient.execute(method);
            url = URLDecoder.decode(url, "UTF-8");

            return buildResult(url, result, noNeedResponse);
        } catch (IOException e) {
            logger.error("通信异常:" + url, e);
        }
        return null;
    }


    private static JSONObject buildResult(String url, HttpResponse result, boolean noNeedResponse) {
        int statusCode = result.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            logger.error("通信异常:code=" + statusCode + "," + url + "," + result.getStatusLine());
            throw new RuntimeException("通信异常");
        }

        /**读取服务器返回过来的json字符串数据**/
        String str;
        try {
            str = EntityUtils.toString(result.getEntity());
        } catch (IOException e) {
            logger.error("获取返回数据异常", e);
            throw new RuntimeException(e);
        }

        if (noNeedResponse) {
            return null;
        }

        /**把json字符串转换成json对象**/
        try {
            return JSONObject.parseObject(str);
        } catch (Exception e) {
            logger.error("JSON转换异常,无法将字符串转换成JSON对象\n" + str, e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 发送get请求
     *
     * @param url 路径
     * @return
     */
    public static JSONObject requestGet(String url) {
        //get请求返回结果
        JSONObject jsonResult = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //发送get请求
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            /**请求发送成功，并得到响应**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**读取服务器返回过来的json字符串数据**/
                String strResult = EntityUtils.toString(response.getEntity());
                /**把json字符串转换成json对象**/
                jsonResult = JSONObject.parseObject(strResult);
                url = URLDecoder.decode(url, "UTF-8");
            } else {
                logger.error("通信异常:" + url);
            }
        } catch (IOException e) {
            logger.error("通信异常:" + url, e);
        }
        return jsonResult;
    }


}

package com.msp.messenger.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 15. 9. 16..
 */
public class HttpClientUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest request;
    private final String REQ_SessionID;
    private CloseableHttpClient httpClient = null;
    private HttpPost httpPost = null;
    private HttpGet httpGet = null;
    private List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    private final String DEFAULT_CHARSET = "UTF-8";
    private String RESPONSE_CHARSET = "UTF-8";

    public final static int GET = 0;
    public final static int POST = 1;
    public final static int PUT =2;
    public final static int DELETE =3;

    public static final RequestConfig requestConfig = RequestConfig.custom()
        .setSocketTimeout(5000)
        .setConnectTimeout(5000)
        .setConnectionRequestTimeout(5000)
        .build();

    private int methodKind = POST;

    public HttpClientUtil(HttpServletRequest request) throws Exception{
        if(request!=null) {
            this.request = request;
            this.REQ_SessionID = getSessionID(request);
        }else{
            this.request = null;
            this.REQ_SessionID = "";
        }
    }

    public HttpClientUtil(HttpServletRequest request, String charset) throws Exception{
        RESPONSE_CHARSET = charset;
        if(request!=null) {
            this.request = request;
            this.REQ_SessionID = getSessionID(request);
        }else{
            this.request = null;
            this.REQ_SessionID = "";
        }
    }

    public void httpPostConnect(String url, Map<String,Object> reqParam, RequestConfig requestConfig) throws Exception{
        if(url!=null && url.length()>5 && url.substring(0,5).toLowerCase().equals("https")){
            httpClient = getHttpsClient(); //SSL 컨넥션
        }else {
            httpClient = getHttpClient();  //일반 컨넥션
        }
        httpPost = getHttpPost(url);

        if(requestConfig!=null) {
            httpPost.setConfig(requestConfig);
        }
        if(reqParam!=null) {
            for (Map.Entry<String, Object> parameter : reqParam.entrySet()) {
                nvps.add(new BasicNameValuePair(parameter.getKey(), parameter.getValue().toString()));
            }
        }
        methodKind = POST;
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, DEFAULT_CHARSET));
    }

    public void httpPostConnect(String url, String rawParam, RequestConfig requestConfig) throws Exception{
        if(url!=null && url.length()>5 && url.substring(0,5).toLowerCase().equals("https")){
            httpClient = getHttpsClient(); //SSL 컨넥션
        }else {
            httpClient = getHttpClient();  //일반 컨넥션
        }
        httpPost = getHttpPost(url);

        if(requestConfig!=null) {
            httpPost.setConfig(requestConfig);
        }

        methodKind = POST;
        httpPost.setEntity(new StringEntity(rawParam));
    }

    public void httpGetConnect(String url,RequestConfig requestConfig) throws Exception{
        methodKind = GET;
        if(url!=null && url.length()>5 && url.substring(0,5).equals("https")){
            httpClient = getHttpsClient(); //SSL 컨넥션
        }else {
            httpClient = getHttpClient();  //일반 컨넥션
        }
        httpGet = getHttpGet(url);

        if(requestConfig!=null) {
            httpGet.setConfig(requestConfig);
        }
    }

    public void httpPostConnect(String url,Map<String,String> addHttpHeadParam, String rawParam, RequestConfig requestConfig) throws Exception{
        if(url!=null && url.length()>5 && url.substring(0,5).toLowerCase().equals("https")){
            httpClient = getHttpsClient(); //SSL 컨넥션
        }else {
            httpClient = getHttpClient();  //일반 컨넥션
        }
        httpPost = getHttpPost(url);
        // 추가 요청 헤더를 넣음
        if(addHttpHeadParam!=null){
            for (Map.Entry<String, String> headParam : addHttpHeadParam.entrySet()) {
                httpPost.addHeader(headParam.getKey(), headParam.getValue().toString());
            }
        }


        if(requestConfig!=null) {
            httpPost.setConfig(requestConfig);
        }
        if(rawParam!=null && rawParam.length()>0) {
            httpPost.setEntity(new StringEntity(rawParam, DEFAULT_CHARSET));
        }
        methodKind = POST;
    }

    public void httpPostConnect(String url,Map<String,String> addHttpHeadParam, Map<String,Object> reqParam, RequestConfig requestConfig) throws Exception{
        if(url!=null && url.length()>5 && url.substring(0,5).toLowerCase().equals("https")){
            httpClient = getHttpsClient(); //SSL 컨넥션
        }else {
            httpClient = getHttpClient();  //일반 컨넥션
        }
        httpPost = getHttpPost(url);
        // 추가 요청 헤더를 넣음
        if(addHttpHeadParam!=null){
            for (Map.Entry<String, String> headParam : addHttpHeadParam.entrySet()) {
                httpPost.addHeader(headParam.getKey(), headParam.getValue().toString());
            }
        }

        if(requestConfig!=null) {
            httpPost.setConfig(requestConfig);
        }
        if(reqParam!=null) {
            for (Map.Entry<String, Object> parameter : reqParam.entrySet()) {
                nvps.add(new BasicNameValuePair(parameter.getKey(), parameter.getValue().toString()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, DEFAULT_CHARSET));
        }
        methodKind = POST;
    }


    /**
     * 파일업로드 Post 방식
     * @param url
     * @param addHttpHeadParam
     * @param reqParam  Map<String,Object> 멀티파트리퀘스트로 넘길 파라미터 맵
     * @param uploadFileMaps List<Map<첨부파일파라미터이름,첨부파일>>형태의 파일맵리스트 파라미터 전달
     * @param requestConfig
     * @throws Exception
     */
    public void httpPostConnect(String url,Map<String,String> addHttpHeadParam, Map<String,Object> reqParam,List<Map<String,File>> uploadFileMaps,RequestConfig requestConfig) throws Exception{
        if(url!=null && url.length()>5 && url.substring(0,5).toLowerCase().equals("https")){
            httpClient = getHttpsClient(); //SSL 컨넥션
        }else {
            httpClient = getHttpClient();  //일반 컨넥션
        }
        httpPost = getHttpPost(url);
        // 추가 요청 헤더를 넣음
        if(addHttpHeadParam!=null){
            for (Map.Entry<String, String> headParam : addHttpHeadParam.entrySet()) {
                httpPost.addHeader(headParam.getKey(), headParam.getValue().toString());
            }
        }

        if(requestConfig!=null) {
            httpPost.setConfig(requestConfig);
        }
        //Multipart Entity Builder 생성
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

        // 파라미터 Entity 추가
        if(reqParam!=null) {
            for (Map.Entry<String, Object> parameter : reqParam.entrySet()) {
                multipartEntityBuilder.addPart(parameter.getKey(),new StringBody(parameter.getValue().toString(), ContentType.TEXT_PLAIN));
            }
        }

        // 파일 Entity 추가
        for(Map<String,File> upFileMap : uploadFileMaps) {
            Set<Map.Entry<String,File>> entrySet = upFileMap.entrySet();
            for(Map.Entry<String,File> mapEntry : entrySet){
                ContentBody fileBody = new FileBody(mapEntry.getValue());
                multipartEntityBuilder.addPart(mapEntry.getKey(),fileBody);
            }
//            multipartEntityBuilder.addBinaryBody("attachment", IOUtils.toByteArray(inputStream), ContentType.APPLICATION_OCTET_STREAM, fileName);
        }

        HttpEntity httpMultipartEntity = multipartEntityBuilder.build();
        if(httpMultipartEntity!=null) {
            httpPost.setEntity(httpMultipartEntity);
        }
        methodKind = POST;

    }

    public void httpGetConnect(String url,Map<String,String> addHttpHeadParam,RequestConfig requestConfig) throws Exception{
        methodKind = GET;
        if(url!=null && url.length()>5 && url.substring(0,5).equals("https")){
            httpClient = getHttpsClient(); //SSL 컨넥션
        }else {
            httpClient = getHttpClient();  //일반 컨넥션
        }
        httpGet = getHttpGet(url);
        // 추가 요청 헤더를 넣음
        if(addHttpHeadParam!=null){
            for (Map.Entry<String, String> headParam : addHttpHeadParam.entrySet()) {
                httpGet.addHeader(headParam.getKey(), headParam.getValue().toString());
            }
        }

        if(requestConfig!=null) {
            httpGet.setConfig(requestConfig);
        }
    }

    public Map<String, Object> sendForJsonResponse(HttpServletResponse response) throws Exception{
        Map<String, Object> responseMap = new HashMap<String, Object>();
        CloseableHttpResponse gateResponse = null;
        if(methodKind==POST) {
            gateResponse = httpClient.execute(httpPost);
        }else{
            gateResponse = httpClient.execute(httpGet);
        }

        Header[] headers = gateResponse.getAllHeaders();
        if(response!=null) {
            for (Header header : headers) {
                logger.debug("[HTTP SERVER] Response Key : " + header.getName() + " ,Value : " + header.getValue());
                if ("Set-Cookie".equals(header.getName())) {
                    // http 서버에서 세션아이드를 재발급 했을 경우 다시 클라이언트에 세션아이디를 알려준다.
                    response.setHeader("Set-Cookie", header.getValue()); // http 연결 서버로 부터 받은 세션아이디를 단말에 넘겨 주기 위해
//                    break;
                }
            }
        }
        int statusCode = gateResponse.getStatusLine().getStatusCode();

        try {
            HttpEntity entity2 = gateResponse.getEntity();
            BufferedReader br = new BufferedReader(new InputStreamReader((entity2.getContent()),RESPONSE_CHARSET));

            StringBuffer buf = new StringBuffer();
            String output;
            while ((output = br.readLine()) != null) {
                buf.append(output);
            }
            String responseContent = buf.toString();
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED) {
                throw new IllegalStateException(gateResponse.getStatusLine()+"\r\n"+responseContent);
            }
            EntityUtils.consume(entity2);
            responseMap = new Gson().fromJson(responseContent, new TypeToken<HashMap<String, Object>>() {}.getType());
        } finally {
            gateResponse.close();
        }

        return responseMap;
    }

    public Map<String,Object> sendForBodyString(HttpServletResponse response) throws Exception{
        Map<String,Object> responseMap = new HashMap<String,Object>();
        String responseContent = "";
        CloseableHttpResponse gateResponse = null;
        if(methodKind==POST) {
            gateResponse = httpClient.execute(httpPost);
        }else{
            gateResponse = httpClient.execute(httpGet);
        }
        Header[] headers = gateResponse.getAllHeaders();
        if(response!=null) {
            for (Header header : headers) {
                logger.debug("[HTTP SERVER] Response Key : " + header.getName() + " ,Value : " + header.getValue());
                if ("Set-Cookie".equals(header.getName())) {
                    // http 서버에서 세션아이드를 재발급 했을 경우 다시 클라이언트에 세션아이디를 알려준다.
                    response.setHeader("Set-Cookie", header.getValue()); // http 연결 서버로 부터 받은 세션아이디를 단말에 넘겨 주기 위해
//                    break;
                }
            }
        }
        int statusCode = gateResponse.getStatusLine().getStatusCode();

        try {
            HttpEntity entity2 = gateResponse.getEntity();
            BufferedReader br = new BufferedReader(new InputStreamReader((entity2.getContent()),RESPONSE_CHARSET));

            StringBuffer buf = new StringBuffer();
            String output;
            while ((output = br.readLine()) != null) {
                buf.append(output);
            }
            responseContent = buf.toString();
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED) {
                throw new IllegalStateException(gateResponse.getStatusLine()+"\r\n"+responseContent);
            }
            EntityUtils.consume(entity2);
        } finally {
            gateResponse.close();
        }
        responseMap.put("statusCode",statusCode);
        responseMap.put("headers", headers);
        responseMap.put(("body"), responseContent);
        return responseMap;
    }

    public File sendForFileDownload(HttpServletResponse response, String tempDownLoadDir) throws Exception{
        File downLoadFile = null;
        CloseableHttpResponse gateResponse = null;
        if(methodKind==POST) {
            gateResponse = httpClient.execute(httpPost);
        }else{
            gateResponse = httpClient.execute(httpGet);
        }
        Header[] headers = gateResponse.getAllHeaders();
        if(response!=null) {
            for (Header header : headers) {
                logger.debug("[HTTP SERVER] Response Key : " + header.getName() + " ,Value : " + header.getValue());
                if ("Set-Cookie".equals(header.getName())) {
                    // http 서버에서 세션아이드를 재발급 했을 경우 다시 클라이언트에 세션아이디를 알려준다.
                    response.setHeader("Set-Cookie", header.getValue()); // http 연결 서버로 부터 받은 세션아이디를 단말에 넘겨 주기 위해
//                    break;
                }
            }
        }
        int statusCode = gateResponse.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED) {
            throw new IllegalStateException("Method failed: " + gateResponse.getStatusLine());
        }
        try {
            HttpEntity entity2 = gateResponse.getEntity();
            BufferedInputStream bis = new BufferedInputStream(entity2.getContent());
            String filepath = tempDownLoadDir;
            downLoadFile = new File(filepath);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(downLoadFile));
            int inByte;
            while((inByte = bis.read()) != -1) {
                bos.write(inByte);
            }
            bis.close();
            bos.close();
            EntityUtils.consume(entity2);
        } finally {
            gateResponse.close();
        }
        return downLoadFile;
    }

    private HttpPost getHttpPost(String url){
        HttpPost httpPost = new HttpPost(url);
        if(!REQ_SessionID.equals("")) {
            httpPost.addHeader("Cookie",REQ_SessionID);
        }
        return httpPost;
    }

    private HttpGet getHttpGet(String url){
        HttpGet httpGet = new HttpGet(url);
        if(!REQ_SessionID.equals("")) {
            httpGet.addHeader("Cookie",REQ_SessionID);
        }
        return httpGet;
    }

    private CloseableHttpClient getHttpClient(){
        httpClient = HttpClients.createDefault();
        return httpClient;
    }

    private CloseableHttpClient getHttpsClient() throws Exception{
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // https일 경우
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        SSLContext sslContext = SSLContext.getInstance("SSL");
        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                logger.debug("getAcceptedIssuers =============");
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs,String authType) {
                logger.debug("checkClientTrusted =============");
            }
            public void checkServerTrusted(X509Certificate[] certs,String authType) {
                logger.debug("checkServerTrusted =============");
            }
        } }, new SecureRandom());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
        httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        return httpClient;
    }
    private String getSessionID(HttpServletRequest request) throws Exception{
        String REQ_JSESSIONID = "";
        //단말로 부터 넘어온 Http header 정보를 파싱하여 SESSIONID를 구한다.
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            if(headerName.toLowerCase().equals("cookie")) {
                for (Enumeration values = request.getHeaders(headerName); values.hasMoreElements();) {
                    String value = (String)values.nextElement();
                    REQ_JSESSIONID = value;
                }
            }
        }
        logger.debug("####### 받은 세션아이디: "+REQ_JSESSIONID);
        return REQ_JSESSIONID.trim();
    }
}

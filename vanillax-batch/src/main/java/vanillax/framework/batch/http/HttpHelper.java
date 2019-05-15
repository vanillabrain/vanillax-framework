/*
 * Copyright (C) 2016 Vanilla Brain, Team - All Rights Reserved
 *
 * This file is part of 'VanillaTopic'
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Vanilla Brain Team and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Vanilla Brain Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vanilla Brain Team.
 */

package vanillax.framework.batch.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.logging.Logger;


/**
 * HTTP 요청을 수행하는 Util
 */
public class HttpHelper {
    private static final Logger log = Logger.getLogger(HttpHelper.class.getName());
    private static final int TIME_OUT_SEC = 5;

    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String ENCODING_KR = "EUC-KR";

    public static String get(String url) throws Exception {
        return get(url, ENCODING_UTF8);
    }

    /**
     * GET 하는 경우
     * @param url 호출할 URL
     * @param encoding 결과 문자 인코딩 값
     * @return HTTP Response 결과
     * @throws Exception HTTP 요청 오류 발생시
     */
    public static String get(String url, String encoding) throws Exception {
        String response = null;
        CloseableHttpClient httpclient = null;
//        encoding = encoding != null ? encoding : ENCODING_UTF8; //check encoding parameter
        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(TIME_OUT_SEC * 1000)
                    .setConnectionRequestTimeout(TIME_OUT_SEC * 1000)
                    .setSocketTimeout(TIME_OUT_SEC * 1000).build();
            httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
            httpget.setHeader("Accept-Language","ko,en-US,en;q=0.8,cy;q=0.6,ko;q=0.4,zh-CN;q=0.2");
//            httpget.setHeader("Accept-Encoding",""); //zlib 처리 등등의 라이브러리 미 구현된 사이트 처리용

            // Create a custom response handler
            ResponseHandler<String> responseHandler = response1 -> {
                int status = response1.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response1.getEntity();
                    return entity != null ? EntityUtils.toString(entity, encoding) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            log.info("Request URL: "+url);
            long startTime = System.currentTimeMillis();
            response = httpclient.execute(httpget, responseHandler);
            long endTime = System.currentTimeMillis();
            if(endTime - startTime > 3 * 1000){
                log.info("HTTP response is time too long : "+(endTime - startTime) );
                log.info("URL: "+url);
            }
        } finally {
            try{httpclient.close();}catch(Exception ignore){}
        }
        return response;
    }

    public static byte[] getBytes(String url) throws Exception {
        byte[] response = null;
        CloseableHttpClient httpclient = null;
        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(TIME_OUT_SEC * 1000)
                    .setConnectionRequestTimeout(TIME_OUT_SEC * 1000)
                    .setSocketTimeout(TIME_OUT_SEC * 1000).build();
            httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
            httpget.setHeader("Accept-Language","ko,en-US,en;q=0.8,cy;q=0.6,ko;q=0.4,zh-CN;q=0.2");
//            httpget.setHeader("Accept-Encoding",""); //zlib 처리 등등의 라이브러리 미 구현된 사이트 처리용

            // Create a custom response handler
            ResponseHandler<?> responseHandler = response1 -> {
                int status = response1.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response1.getEntity();
                    return entity != null ? EntityUtils.toByteArray(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            log.info("Request URL: "+url);
            long startTime = System.currentTimeMillis();
            response = (byte[]) httpclient.execute(httpget, responseHandler);
            long endTime = System.currentTimeMillis();
            if(endTime - startTime > 3 * 1000){
                log.info("HTTP response is time too long : "+(endTime - startTime) );
                log.info("URL: "+url);
            }
        } finally {
            try{httpclient.close();}catch(Exception ignore){}
        }
        return response;
    }

    public static String getEncoded(String url,String charSet) throws Exception {
        byte[] response = getBytes(url);
        if(response == null)
            return null;
        return new String(response, charSet);
    }

    /**
     * json 타입으로 POST 하는 경우
     * @param url 요청 대상 URL
     * @param body 요청 Content
     * @return HTTP 결과 body 값
     * @throws Exception HTTP 요청 오류발시 돌출
     */
    public static String postJson(String url, String body) throws Exception {
        String response;
        CloseableHttpClient httpclient = null;
        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(TIME_OUT_SEC * 1000)
                    .setConnectionRequestTimeout(TIME_OUT_SEC * 1000)
                    .setSocketTimeout(TIME_OUT_SEC * 1000).build();
            httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(body);
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Content-type", "application/json");

            // Create a custom response handler
            ResponseHandler<String> responseHandler = response1 -> {
                int status = response1.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response1.getEntity();
                    return entity != null ? EntityUtils.toString(entity, HttpHelper.ENCODING_UTF8) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            log.info("Request URL: "+url);
            long startTime = System.currentTimeMillis();
            response = httpclient.execute(httpPost, responseHandler);
            long endTime = System.currentTimeMillis();
            if(endTime - startTime > 3 * 1000){
                log.info("HTTP response is time too long : "+(endTime - startTime) );
                log.info("URL: "+url);
            }
        } finally {
            try{httpclient.close();}catch(Exception ignore){}
        }
        return response;
    }
}

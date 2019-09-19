package com.ddyh.product.service.common.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class WeixinUtil {

    private static Logger log = LoggerFactory.getLogger(WeixinUtil.class);

    public static final String WEIXIN_WEB_APPID = "wx16b79b088aef3a5d";
    public static final String WEIXIN_WEB_SECRET = "4ca7681f2e4f779edca008be08cc8cee";

    protected static final String BASE_URI = "https://api.weixin.qq.com";

    private static LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();
    private static LinkedHashMap<String, String> ticketMap = new LinkedHashMap<String, String>();

    static {
        //重启时需要调用
        init();
    }

    /**
     * 调用微信接口，获取token
     *
     */
    public static void init() {
        Token token = token(WEIXIN_WEB_APPID, WEIXIN_WEB_SECRET);
        log.info("############################access_token:" + token.getAccess_token());
        tokenMap.put(WEIXIN_WEB_APPID, token.getAccess_token());
        Ticket ticket = ticketGetticket(token.getAccess_token());
        ticketMap.put(WEIXIN_WEB_APPID, ticket.getTicket());
        log.info("############################ticket:" + ticket.getTicket());
    }

    /**
     * 获取第一个appid 的  jsapi ticket
     * 适用于单一微信号
     *
     * @return
     */
    public static String getDefaultTicket() {
        Object[] objs = ticketMap.values().toArray();
        return objs.length > 0 ? objs[0].toString() : null;
    }


    public static Ticket ticketGetticket(String access_token) {
        HttpUriRequest httpUriRequest = RequestBuilder.get()
                .setUri(BASE_URI + "/cgi-bin/ticket/getticket")
                .addParameter("access_token", access_token)
                .addParameter("type", "jsapi")
                .build();
        return LocalHttpClient.executeJsonResult(httpUriRequest, Ticket.class);
    }


    public static String generateConfigSignature(String noncestr, String jsapi_ticket, String timestamp, String url) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("noncestr", noncestr);
        map.put("jsapi_ticket", jsapi_ticket);
        map.put("timestamp", timestamp);
        map.put("url", url);
        return generatePaySign(map, null);
    }

    public static String generatePaySign(Map<String, String> map, String paySignKey) {
        if (paySignKey != null) {
            map.put("appkey", paySignKey);
        }
        Map<String, String> tmap = MapUtil.order(map);
        String str = MapUtil.mapJoin(tmap, true, false);
        return DigestUtils.shaHex(str);
    }

    //*********************************下面的所有内部类都必须用static，否则会报错*******************************************************************************//

    public static class Ticket {
        private String ticket;

        private Integer expires_in;

        private String errcode;
        private String errmsg;

        public String getErrcode() {
            return errcode;
        }

        public void setErrcode(String errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }


        public String getTicket() {
            return ticket;
        }

        public void setTicket(String ticket) {
            this.ticket = ticket;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }
    }


    public static class Token {

        private String access_token;
        private int expires_in;

        private String errcode;
        private String errmsg;

        public String getErrcode() {
            return errcode;
        }

        public void setErrcode(String errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String accessToken) {
            access_token = accessToken;
        }

        public int getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(int expiresIn) {
            expires_in = expiresIn;
        }
    }

    public static Token token(String appid, String secret) {
        HttpUriRequest httpUriRequest = RequestBuilder.get()
                .setUri(BASE_URI + "/cgi-bin/token")
                .addParameter("grant_type", "client_credential")
                .addParameter("appid", appid)
                .addParameter("secret", secret)
                .build();
        return LocalHttpClient.executeJsonResult(httpUriRequest, Token.class);
    }


    public static class LocalHttpClient {
        protected static HttpClient httpClient = createHttpClient(100, 10);
        private static Map<String, HttpClient> httpClient_mchKeyStore = new HashMap<String, HttpClient>();

        public static void init(int maxTotal, int maxPerRoute) {
            httpClient = createHttpClient(maxTotal, maxPerRoute);
        }

        public static HttpResponse execute(HttpUriRequest request) {
            try {
                return httpClient.execute(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static <T> T execute(HttpUriRequest request, ResponseHandler<T> responseHandler) {
            try {
                return httpClient.execute(request, responseHandler);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 数据返回自动JSON对象解析
         *
         * @param request
         * @param clazz
         * @return
         */
        public static <T> T executeJsonResult(HttpUriRequest request, Class<T> clazz) {
            return execute(request, JsonResponseHandler.createResponseHandler(clazz));
        }

        public static HttpClient createHttpClient(int maxTotal, int maxPerRoute) {
            try {
                SSLContext sslContext = SSLContexts.custom().useSSL().build();
                SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
                poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
                poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
                return HttpClientBuilder.create()
                        .setConnectionManager(poolingHttpClientConnectionManager)
                        .setSSLSocketFactory(sf)
                        .build();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class JsonResponseHandler {
        private static Map<String, ResponseHandler<?>> map = new HashMap<String, ResponseHandler<?>>();

        public static <T> ResponseHandler<T> createResponseHandler(final Class<T> clazz) {

            if (map.containsKey(clazz.getName())) {
                return (ResponseHandler<T>) map.get(clazz.getName());
            } else {
                ResponseHandler<T> responseHandler = new ResponseHandler<T>() {
                    @Override
                    public T handleResponse(HttpResponse response)
                            throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = response.getEntity();
                            String str = EntityUtils.toString(entity, "utf-8");
                            return parseObject(new String(str.getBytes("utf-8"), "utf-8"), clazz);
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };
                map.put(clazz.getName(), responseHandler);
                return responseHandler;
            }
        }

        public static <T> T parseObject(String json, Class<T> clazz) {
            return JSON.parseObject(json, clazz);
        }
    }


}

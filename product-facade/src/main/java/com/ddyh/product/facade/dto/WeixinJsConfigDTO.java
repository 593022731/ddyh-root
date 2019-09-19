package com.ddyh.product.facade.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信jsoConfig相关DTO
 */
public class WeixinJsConfigDTO implements Serializable {
    private boolean debug;
    private String appId;
    private long timestamp;
    private String nonceStr;
    private String signature;
    private List<String> jsApiList;

    public WeixinJsConfigDTO(List<String> jsApiList) {
        if (jsApiList == null || jsApiList.size() <= 0) {
            this.jsApiList = new ArrayList<>();
            this.jsApiList.add("checkJsApi");
            this.jsApiList.add("onMenuShareTimeline");
            this.jsApiList.add("onMenuShareAppMessage");
            this.jsApiList.add("onMenuShareQQ");
            this.jsApiList.add("onMenuShareWeibo");
            this.jsApiList.add("onMenuShareQZone");
            this.jsApiList.add("hideMenuItems");
            this.jsApiList.add("showMenuItems");
            this.jsApiList.add("hideAllNonBaseMenuItem");
            this.jsApiList.add("showAllNonBaseMenuItem");
            this.jsApiList.add("translateVoice");
            this.jsApiList.add("startRecord");
            this.jsApiList.add("stopRecord");
            this.jsApiList.add("onVoiceRecordEnd");
            this.jsApiList.add("playVoice");
            this.jsApiList.add("onVoicePlayEnd");
            this.jsApiList.add("pauseVoice");
            this.jsApiList.add("stopVoice");
            this.jsApiList.add("uploadVoice");
            this.jsApiList.add("downloadVoice");
            this.jsApiList.add("chooseImage");
            this.jsApiList.add("previewImage");
            this.jsApiList.add("uploadImage");
            this.jsApiList.add("downloadImage");
            this.jsApiList.add("getNetworkType");
            this.jsApiList.add("openLocation");
            this.jsApiList.add("getLocation");
            this.jsApiList.add("hideOptionMenu");
            this.jsApiList.add("showOptionMenu");
            this.jsApiList.add("closeWindow");
            this.jsApiList.add("scanQRCode");
            this.jsApiList.add("chooseWXPay");
            this.jsApiList.add("openProductSpecificView");
            this.jsApiList.add("addCard");
            this.jsApiList.add("chooseCard");
            this.jsApiList.add("openCard");
        } else {
            this.jsApiList = jsApiList;
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getJsApiList() {
        return jsApiList;
    }

    public void setJsApiList(List<String> jsApiList) {
        this.jsApiList = jsApiList;
    }
}

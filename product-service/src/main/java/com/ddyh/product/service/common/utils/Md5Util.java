package com.ddyh.product.service.common.utils;

import java.security.MessageDigest;

/**
 * @author: cqry2017
 * @Date: 2019/6/15 16:39
 */
public class Md5Util {

    /**
     * md5,32位小写
     */
    public static String md5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("md5");
            byte[] byteArray = inStr.getBytes("utf-8");

            byte[] md5Bytes = md5.digest(byteArray);

            StringBuffer hexValue = new StringBuffer();

            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }

            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

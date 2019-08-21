package com.ddyh.pay.service.util;

import com.ddyh.commons.utils.MD5Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

/**
 * 微信工具类
 * @author: weihui
 * @Date: 2019/8/21 10:41
 */
public class WXUtil {

    /**
     * 微信支付签名算法sign
     * @param parameters
     * @param key
     * @return
     */
    public static String createSign(SortedMap<Object, Object> parameters, String key) {
        StringBuffer sb = new StringBuffer();
        // 所有参与传参的参数按照accsii排序（升序）
        parameters.forEach((k, v) -> {
            if (!"sign".equals(k) && !"key".equals(k) && null != v && !"".equals(v)) {
                sb.append(k + "=" + v + "&");
            }
        });
        sb.append("key=" + key);
        String sign = MD5Utils.GetMD5Code(sb.toString()).toUpperCase();
        return sign;
    }

    /**
     * 将请求参数转换为xml
     * @param parameters
     * @return
     */
    public static String getRequestXml(SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        parameters.forEach((k,v) -> {
            if ("attach".equalsIgnoreCase((String) k) || "body".equalsIgnoreCase((String) k)
                    || "sign".equalsIgnoreCase((String) k)) {
                sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
            } else {
                sb.append("<" + k + ">" + v + "</" + k + ">");
            }
        });
        sb.append("</xml>");
        try {
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 返回给微信的参数
     * @param return_code
     * @param return_msg
     * @return
     */
    public static String setXML(String return_code, String return_msg) {
        return "<xml><return_code><![CDATA[" + return_code
                + "]]></return_code><return_msg><![CDATA[" + return_msg
                + "]]></return_msg></xml>";
    }

    /**
     * 解析xml字符串转成map集合
     *
     * @param xml
     * @return
     */
    public static Map<String, String> doXMLParse(String xml) {
        Map<String, String> map = new HashMap<>(12);
        // 将编码改为UTF-8,并去掉换行符\空格等
        xml = xml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
        //去掉空白 换行符
        final StringBuilder sb = new StringBuilder(xml.length());
        char c;
        for (int i = 0; i < xml.length(); i++) {
            c = xml.charAt(i);
            if (c != '\n' && c != '\r' && c != ' ') {
                sb.append(c);
            }
        }
        xml = sb.toString();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            StringReader reader = new StringReader(xml);
            InputSource inputSource = new InputSource(reader);
            Document document = documentBuilder.parse(inputSource);
            // 1.获取xml文件的根元素
            Element element = document.getDocumentElement();
            // 2.获取根元素下的所有子标签
            NodeList nodeList = element.getChildNodes();
            // 3.遍历子标签集合
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                map.put(node.getNodeName(), node.getFirstChild().getNodeValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}

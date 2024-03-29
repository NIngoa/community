package com.nowcoder.community.utils;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    //生成一个随机字符串
    public static String getUUID(){
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }

    //MD5加密
    public static String md5(String key){
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    //解析json字符串，返回相应对象
    public static <T> T getObj(Object o, Class<T> clazz){
        if (o==null){
            return null;
        }
        String string = JSONObject.toJSONString(o);
        return JSONObject.parseObject(string, clazz);
    }

    public static String getJsonStr(int code, String msg, Map<String,Object>map){
        JSONObject json=new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map!=null){
            for (String key : map.keySet()) {
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }
    public static String getJsonStr(int code, String msg){
        return getJsonStr(code,msg,null);
    }

    public static String getJsonStr(int code){
        return getJsonStr(code,null,null);
    }


}

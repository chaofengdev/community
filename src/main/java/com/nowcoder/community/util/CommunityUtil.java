package com.nowcoder.community.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jdk.dynalink.beans.StaticClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    //利用java.util包下的UUID【生成随机字符串】
    public static String generateUUID() {
        //直接使用UUID.randomUUID().toString()生成随机字符串，但是里面有‘-’字符，使用replace去掉该字符
        return UUID.randomUUID().toString().replace("-","");
    }

    //MD5加密
    //输入用户密码，输出加密后的密码
    //因为MD5加密算法本身的局限性，所以我们在密码后加上salt，生成密码
    //hello -> abc123def456
    //hello + salt -> abc123def456zzz
    /**
     * MD5加密是一种不可逆的加密算法，不可逆加密算法的特征是加密过程中不需要使用密钥，输入明文后由系统直接经过加密算法处理成密文，这种加密后的数据是无法被解密的，
     * 只有重新输入明文，并再次经过同样不可逆的加密算法处理，得到相同的加密密文并被系统重新识别后，才能真正解密。
     * 具体加密原理详见：https://zhuanlan.zhihu.com/p/37257569  这部分属于网络安全或者密码学的知识点，本科略有涉及，对称加密非对称加密这些
     */
    public static String md5(String key) {
        //为了代码的健壮性，利用commons lang3下的StringUtils.isBlank(key)方法，判断key是否为空，查看源码可知，这里的判空是指字符串中的字符不能出现空字符，如空格换行或者制表符，也就是"123 456"这种密码是不被允许的。
        //org.apache.commons.lang.StringUtils中方法的操作对象是java.lang.String类型的对象，是JDK提供的String类型操作方法的补充，并且是null安全的(即如果输入参数String为null则不会抛出NullPointerException，而是做了相应处理，例如，如果输入为null则返回也是null等，具体可以查看源代码)。
        if(StringUtils.isBlank(key)) {
            return null;
        }
        //利用spring自带的工具，DigestUtils.md5DigestAsHex()将密码通过md5算法加密为十六进制的字符串，注意这里需要传入byte[]类型，所以需要String到byte[]类型的转换
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 利用json对象封装相关数据，返回json字符串
     * 浏览器得到服务器传来的json字符串，转换为JavaScript对象，得到每个key-value
     * @param code
     * @param msg
     * @param map
     * @return json格式的字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        //新建json对象
        JSONObject json = new JSONObject();
        //json对象封装相关数据
        json.put("code",code);
        json.put("msg",msg);
        if(map != null) {
            for(String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        //返回字符串
        return json.toJSONString();//将Java对象序列化为JSON字符串
    }

    /**
     * getJSONString重载1
     * @param code
     * @param msg
     * @return
     */
    public static String getJSONString(int code, String msg) {
        return getJSONString(code,msg,null);
    }

    /**
     * getJSONString重载2
     * @param code
     * @return
     */
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    //简单测试一下几个工具类。
    //因为工具类不需要注入到spring中，所以在这里简单测试。
    public static void main (String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "chenchaofeng");
        map.put("age", 99);
        System.out.println(getJSONString(0, "ok", map));
    }
}

package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    /**
     * 根据request找到cookie中某个name对应的value，没找到返回null
     * 具体来说是键值对("ticket", "xxxxxx")。
     * @param request
     * @param name
     * @return
     */
    public static String getValue(HttpServletRequest request, String name) {
        if(request == null || name == null) {
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {//根据name
                    return cookie.getValue();//获取value
                }
            }
        }
        return null;//没有找到cookie
    }
}

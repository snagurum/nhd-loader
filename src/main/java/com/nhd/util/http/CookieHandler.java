package com.nhd.util.http;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookieHandler {
 
    private Map<String, String> cookiesMap = new HashMap<>();

    public void saveCookies(Map<String,List<String>> headers){
        List<String> cookieList = headers.get("Set-Cookie");
        cookieList.forEach(e->{
            String key = e.substring(0,e.indexOf("="));
            String value = e.substring(e.indexOf("=")+1 ,e.indexOf(";"));
            if(value != null && !"".equals(value.trim()))
                cookiesMap.put(key, value);
        });
    }

    public String getCookiesAsString(){
        StringBuffer temp = new StringBuffer();
        cookiesMap.keySet().forEach(e->{
            temp.append(e) ;
            temp.append("=");
            temp.append(cookiesMap.get(e));
            temp.append(";");
        });
        return temp.toString();
    }

    

    public String printCookies(){
        String pretext = "\n\t***\t     ";
        StringBuffer buffer = new StringBuffer();
        cookiesMap.keySet().forEach(e->{
            buffer.append(pretext + e + " = " + cookiesMap.get(e));
        });
        return buffer.toString();
    }

    public Map<String, String> getCookiesMap(){
        return cookiesMap;
    }
}

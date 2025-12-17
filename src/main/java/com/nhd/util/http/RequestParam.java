package com.nhd.util.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class RequestParam{

    private Map<String,String> globalParams = new HashMap<>();

    private List<Map<String,String>> oneTimePageParams = new ArrayList<>();

    public RequestParam(){

    }

    public String applyParams(String url,String pageName){
        String newUrl = url;

        for (String temp : globalParams.keySet()) {
            newUrl = newUrl.replaceAll(temp, globalParams.get(temp));
        }
        // globalParams.keySet().forEach(item -> {
        //     newUrl = newUrl.replaceAll(item, globalParams.get(item));
        // });
        return newUrl;
    }

    public String applyOneTimePageParams(String url){
        String newUrl = url ;

        Map<String,String> params = oneTimePageParams.size()>0 ? oneTimePageParams.get(0):null;

        if(params != null){
            for (String temp : params.keySet()) {
                newUrl = newUrl.replaceAll(temp, params.get(temp));
            }
            // globalParams.keySet().forEach(item -> {
            //     newUrl = newUrl.replaceAll(item, params.get(item));
            // });
            oneTimePageParams = oneTimePageParams.subList(1,oneTimePageParams.size());
        }
        return newUrl;
    }


}
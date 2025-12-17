package com.nhd.util.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class HttpProcessor {

    private List<HttpRequest> httpRequests;
    private String jobName;
    private CookieHandler cookies = new CookieHandler() ;
    private Boolean jobCompleted = false;
    private Boolean collate = false;

    public Map<String,String> getOutput(){
        if(!jobCompleted)
            return null;

        Map<String,String> outputMap  = new HashMap<>();
        if(collate){
            StringBuilder strBuf = new StringBuilder();
            for(HttpRequest httpRequest: httpRequests){
                if(!httpRequest.getOutput().isEmpty())
                    strBuf.append(httpRequest.getOutput().get(httpRequest.getOutputKey()));
            }
            outputMap.put("output",strBuf.toString());
        }else{
            for(HttpRequest httpRequest: httpRequests){
                if(!httpRequest.getOutput().isEmpty())
                    outputMap.putAll(httpRequest.getOutput());
            }
        }

        return outputMap;
    }

}
package com.nhd.util.http;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class HttpRequest {

    private String pageName;
    private String description;
    private String url;
    private Boolean trackCookie = true;
    private Map<String,String> output = new HashMap<>();
    private String outputKey = "output";
    private Boolean retrieveOutput = false;
    private Boolean looping = false;

    public void setOutput(String value){
        output.put(outputKey,value);
    }

}

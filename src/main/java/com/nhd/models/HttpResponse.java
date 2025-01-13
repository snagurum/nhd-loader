package com.nhd.models;

import lombok.Data;

@Data
public class HttpResponse{

    private String responseCode;
    private String responseBody;

    public HttpResponse(int code, String body){
        this.responseBody = body;
        this.responseCode = String.valueOf(code);
    }

}
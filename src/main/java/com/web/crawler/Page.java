package com.web.crawler;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@ToString
@NoArgsConstructor 
public class Page  implements Serializable{
    private String pageName;
    private String url;
    private String method = "GET";
    private Map<String,String> headers;
    private String validationString;
    private Integer validationStatusCode;
    private Integer timeout = 10;
    private Boolean collate = false;
    private String referer;
    private Map<String,String> params;
    private List<PageParam> pageParams;
       
}

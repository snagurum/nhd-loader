package com.web.crawler;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor 
@ToString
public class Bot implements Serializable{

    private String name;
    private String description;
    private Integer timeout;
    private List<Page> pages;
    private Map<String,String> gParam;


}

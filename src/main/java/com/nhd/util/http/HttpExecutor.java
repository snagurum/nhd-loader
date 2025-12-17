package com.nhd.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;



public class HttpExecutor{
    
    private String yamlFile;
    private String ticker;
    private HttpProcessor httpProcessor;
    private RequestParam requestParam;

    private static final Logger log = LoggerFactory.getLogger(HttpExecutor.class);

    public HttpExecutor(String yamlFile){
        this.yamlFile = yamlFile;
    }

    public HttpExecutor(String yamlFile, RequestParam requestParam){
        this.requestParam = requestParam;
        this.yamlFile = yamlFile;
    }

    private void loadYamlFile(){
        Yaml yaml = new Yaml(new Constructor(HttpProcessor.class,new LoaderOptions()));
        InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream(this.yamlFile);
        this.httpProcessor = yaml.load(inputStream);
    }

    public void buildRepeatingRequest(){
        List<HttpRequest> newHttpRequests = new ArrayList<>();
        for(HttpRequest httpRequest: httpProcessor.getHttpRequests()){
            if(httpRequest.getLooping() == true){
                HttpRequest hrequest = new HttpRequest();
                hrequest.setPageName(httpRequest.getPageName());
                hrequest.setDescription(httpRequest.getDescription());
                hrequest.setUrl(httpRequest.getUrl());
                hrequest.setTrackCookie(httpRequest.getTrackCookie());
                newHttpRequests.add(httpRequest);
            }else{
                newHttpRequests.add(httpRequest);
            }            
        }
        httpProcessor.setHttpRequests(newHttpRequests);
    }

    public void process(){
        loadYamlFile();
        List<HttpRequest> httpRequests = httpProcessor.getHttpRequests();
        for(HttpRequest httpRequest: httpRequests){
            try {
                String data = Http.loadPage(
                    this.getUrl(httpRequest.getUrl(),ticker)
                    , httpProcessor.getCookies()
                ).getResponseBody();

                if(httpRequest.getRetrieveOutput()){
                    httpRequest.setOutput(data);
                }
            } catch (IOException ex) {
                log.error(httpProcessor.getJobName()+ ": " 
                    + httpRequest.getPageName()+": "
                    + ex.getMessage(),ex);
                httpProcessor.setJobCompleted(false);
                return;
            }
        }
        httpProcessor.setJobCompleted(true);
    }

    private String getUrl(String url, String ticker) throws UnsupportedEncodingException{
        if(ticker!=null)
            return url.replaceAll("##ticker##", URLEncoder.encode(ticker,"UTF-8"));
        else return url;
    }

    public Map<String,String> getOutput(){
        return this.httpProcessor.getOutput();
    }

    public Boolean isJobCompleted (){
        return this.httpProcessor.getJobCompleted();
    }
}
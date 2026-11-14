package com.nhd.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "loader.crawler")
public class LoaderCrawlerProperties {

    private int connectTimeoutSeconds = 10;
    private int defaultRequestTimeoutSeconds = 10;
    private int requestDelayMillis = 500;
    private int dspRequestDelayMillis = 1000;
    private int dspProgressLogInterval = 100;

}

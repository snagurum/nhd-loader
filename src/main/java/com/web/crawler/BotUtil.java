package com.web.crawler;

import com.nhd.config.LoaderCrawlerProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Map;

public class BotUtil {

    private static final Logger log = LoggerFactory.getLogger(BotUtil.class);

    private Bot bot;
    Map<String,String> gParam;
    private LoaderCrawlerProperties crawlerProperties;

    public BotUtil(Bot bot, LoaderCrawlerProperties crawlerProperties){
        this(bot, null, crawlerProperties);
    }

    public BotUtil(Bot bot, Map<String,String> gParam, LoaderCrawlerProperties crawlerProperties){
        this.bot = bot;
        this.gParam = gParam;
        this.crawlerProperties = crawlerProperties;
    }
    
    public void process() throws PageLoadException, IOException, InterruptedException{
        HttpClient client = HttpUtil.getHttpClient(crawlerProperties.getConnectTimeoutSeconds());

        for( Page eachPage: this.bot.getPages()){
            HttpUtil httpUtil = new HttpUtil(eachPage, gParam, getRequestTimeoutSeconds(eachPage));
            httpUtil.process(client);
            BotUtil.sleepQuietly(crawlerProperties.getRequestDelayMillis());
        }
    }

    private int getRequestTimeoutSeconds(Page page) {
        if (page.getTimeout() != null) {
            return page.getTimeout();
        }
        if (bot.getTimeout() != null) {
            return bot.getTimeout();
        }
        return crawlerProperties.getDefaultRequestTimeoutSeconds();
    }

    public static void sleepQuietly(int i) {
    try {
        Thread.sleep(i);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
}

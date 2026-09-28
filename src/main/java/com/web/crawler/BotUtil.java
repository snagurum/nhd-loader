package com.web.crawler;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Map;

public class BotUtil {

    private Bot bot;
    Map<String,String> gParam;

    public BotUtil(Bot bot){
        this(bot,null);
    }

    public BotUtil(Bot bot, Map<String,String> gParam){
        this.bot = bot;
        this.gParam = gParam ;
    }
    
    public void process() throws PageLoadException, IOException, InterruptedException{
        HttpClient client = HttpUtil.getHttpClient();

        for( Page eachPage: this.bot.getPages()){
            HttpUtil httpUtil = new HttpUtil(eachPage,gParam);
            httpUtil.process(client);
            BotUtil.sleepQuietly(1000);
        }
    }

    public static void sleepQuietly(int i) {
    try {
        Thread.sleep(i);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
}

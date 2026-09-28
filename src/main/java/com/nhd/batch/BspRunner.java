package com.web.nhd.batch;


import com.web.crawler.Bot;
import com.web.crawler.BotUtil;
import com.web.crawler.PageLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import org.apache.commons.lang3.SerializationUtils;

public class BspRunner {

    private static final Bot BSP_BOT;
    static {
        
        Yaml yaml = new Yaml(new Constructor(Bot.class, new LoaderOptions()));
        InputStream inputStream = BspRunner.class.getClassLoader().getResourceAsStream("yamlConfigs/bsp.yaml");
         BSP_BOT = yaml.load(inputStream);
        System.out.println(BSP_BOT);       
    }

    public BspRunner(){ }
    
    public void runJob(){
        System.out.println("-------------------------------------");

        try {
            Bot dspBot = SerializationUtils.clone(BSP_BOT);
            Map<String,String> gParam = Map.of("$gParam1$","INFY");
            BotUtil botUtil = new BotUtil(dspBot, gParam);
            botUtil.process();
        } catch (PageLoadException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getDates(String ticker){
        List<String> availableDates = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime("01-01-2010");
        int startYear = calendar.get(Calendar.YEAR);
        Date endDate = new Date();
        calendar.setTime(endDate);
        int endYear = calendar.get(Calendar.YEAR);

        for(int i=startYear;i<=endYear;i++){
            availableDates.add("01-01-"+i+"__31-12-"+i);
        }
        return availableDates;
    }


}

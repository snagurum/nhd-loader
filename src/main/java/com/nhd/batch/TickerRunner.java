package com.nhd.batch;


import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.nhd.models.JobStatus;
import com.nhd.models.LoadTicker;
import com.nhd.service.AuditService;
import com.nhd.service.StockService;
import com.nhd.util.JobName;
import com.web.crawler.Bot;
import com.web.crawler.BotUtil;
import com.web.crawler.Page;
import com.web.crawler.PageLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component 
public class TickerRunner {
    
    private static final Logger log = LoggerFactory.getLogger(TickerRunner.class);

    private static final Bot TICKER_BOT;
    static {
        
        Yaml yaml = new Yaml(new Constructor(Bot.class, new LoaderOptions()));
        InputStream inputStream = TickerRunner.class.getClassLoader().getResourceAsStream("yamlConfigs/ticker.yaml");
         TICKER_BOT = yaml.load(inputStream);
        log.info(TICKER_BOT.toString());       
    }

    @Autowired
    private StockService stockService ;

    @Autowired
    private AuditService auditService ;
    
    @Scheduled( cron = "#{${loader.ticker.scheduler.cron}}")
    public void runJob(){
        log.trace("method tickerRunner.runJob start");
        JobStatus audit = auditService.startJob(JobName.TICKER);

        try {
            Bot bot = runJob0();
            List<LoadTicker> tickers = buildObjects(bot);
            stockService.saveAllLoadTickers(tickers );
            log.info("loaded tickers count = {}", tickers.size());
            audit.setSuccessCount(tickers.size());
            auditService.endJob(audit);
        } catch (PageLoadException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        log.trace("method tickerRunner.runJob end");
    }

    public Bot runJob0() throws PageLoadException, IOException, InterruptedException{
        Bot tickerBot = SerializationUtils.clone(TICKER_BOT);
        BotUtil botUtil = new BotUtil(tickerBot);
        botUtil.process();
        return tickerBot;
    }

    public List<LoadTicker> buildObjects(Bot bot){
        List<LoadTicker> tickers = new ArrayList<>();
        for(Page page: bot.getPages()){
            if(page.getPersistantData())
                tickers.addAll(this.loadObjectList(page.getResponseData()));
        }
        return tickers;

    }

    public List<LoadTicker> loadObjectList(String dataString) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.builder()
                .addColumn("SYMBOL")
                .addColumn("NAME OF COMPANY")
                .addColumn("SERIES")
                .addColumn("DATE OF LISTING")
                .addColumn("PAID UP VALUE")
                .addColumn("MARKET LOT")
                .addColumn("ISIN NUMBER")
                .addColumn("FACE VALUE")
                .build()
                .withHeader();
            MappingIterator<LoadTicker> it = csvMapper.readerFor(LoadTicker.class).with(schema).readValues(dataString);
            return it.readAll();
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file {}", dataString, e);
            return Collections.emptyList();
        }
    }

}

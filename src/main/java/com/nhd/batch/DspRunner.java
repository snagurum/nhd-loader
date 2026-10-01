package com.nhd.batch;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhd.models.JobStatus;
import com.nhd.models.LoadBspTicker;
import com.nhd.models.LoadDspTicker;
import com.nhd.models.Stock;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

@Component 
public class DspRunner {

    private static final Logger log = LoggerFactory.getLogger(DspRunner.class);

    private static final Bot DSP_BOT;
    
    static {        
        Yaml yaml = new Yaml(new Constructor(Bot.class, new LoaderOptions()));
        InputStream inputStream = DspRunner.class.getClassLoader().getResourceAsStream("yamlConfigs/dsp.yaml");
         DSP_BOT = yaml.load(inputStream);
        log.info(DSP_BOT.toString());       
    }
    
    @Autowired
    private StockService stockService;

    @Autowired
    private AuditService auditService;
    
    
    @Scheduled(cron="#{${loader.dsp_ticker.scheduler.cron}}")
    public void runJob(){
 
        if (!auditService.getTodaysJobStatusByJobName(String.valueOf(JobName.DSP_TICKER)).isEmpty()) return;        
        long jobStart = System.currentTimeMillis();
        int count = 0;
        
        JobStatus audit = auditService.startJob(JobName.DSP_TICKER);
        log.info("DSP job started: {}", audit);

        List<Stock> stocks = stockService.getActiveTickers();
        Map<String, LoadDspTicker> processed = new HashMap<>();

        for (Stock stock : stocks) {
            long tickerStart = System.currentTimeMillis();

            try {
                Bot bot = runJob0(stock);
                List<LoadDspTicker> loadDspTickers = buildObjects(bot, stock);
                processed.put(stock.getTicker(), loadDspTickers.get(0));
                log.info("DSP ticker={} took {} ms (#{})",stock.getTicker(),(System.currentTimeMillis() - tickerStart),++count);

                BotUtil.sleepQuietly(1000);
                if (count % 100 == 0) {
                    long elapsed = System.currentTimeMillis() - jobStart;
                    long avg = elapsed / count;
                    log.info("DSP progress: {} tickers, elapsed={} sec, avg={} ms/ticker", count, (elapsed / 1000), avg);
                }
            } catch (PageLoadException | IOException | InterruptedException e) {
                log.error("dsp stock "+stock.getTicker()+" load error", e);
            }
        }

        stockService.saveAllLoadDspTickers(new ArrayList<>(processed.values()));
        auditService.endJobWithSuccessFailureCount(audit, processed.size(), (stocks.size() - processed.size()));

        long totalTime = System.currentTimeMillis() - jobStart;
        log.info("DSP job completed: total={} sec, avg={} ms/ticker",totalTime / 1000,(totalTime / Math.max(1, stocks.size())));
    }


    private Bot runJob0(Stock stock) throws PageLoadException, IOException, InterruptedException{
                Bot dspBot = SerializationUtils.clone(DSP_BOT);
                Map<String, String> gParam = new HashMap<>(Map.of("$gParam1$", stock.getTicker()));
                BotUtil botUtil = new BotUtil(dspBot, gParam);
                botUtil.process();
                return dspBot;
    }

    public List<LoadDspTicker> buildObjects(Bot bot,Stock stock) throws IOException {
        List<LoadDspTicker> allLoadDspTickers = new ArrayList<>();

        for (Page page : bot.getPages()) {
            if(page.getPersistantData()){
                LoadDspTicker loadDspTickers = new LoadDspTicker();
                loadDspTickers.setTicker(stock.getTicker());
                loadDspTickers.setCompanyDetails(page.getResponseData()); 
                allLoadDspTickers.add(loadDspTickers);
            }
        }
        
        return allLoadDspTickers;
    }
}

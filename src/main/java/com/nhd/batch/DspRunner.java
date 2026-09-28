package com.nhd.batch;


import com.nhd.models.JobStatus;
import com.nhd.models.LoadDspTickers;
import com.nhd.models.Stock;
import com.nhd.service.AuditService;
import com.nhd.service.StockService;
import com.nhd.util.JobName;
import com.web.crawler.Bot;
import com.web.crawler.BotUtil;
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
import org.slf4j.Logger;

public class DspRunner {

    private static final Logger log = LoggerFactory.getLogger(DspRunner.class);

    private static final Bot DSP_BOT;
    
    static {
        
        Yaml yaml = new Yaml(new Constructor(Bot.class, new LoaderOptions()));
        InputStream inputStream = DspRunner.class.getClassLoader().getResourceAsStream("yamlConfigs/dsp.yaml");
         DSP_BOT = yaml.load(inputStream);
        System.out.println(DSP_BOT);       
    }

    
    @Autowired
    private StockService stockService;

    @Autowired
    private AuditService auditService;
    
    public DspRunner(){ }
    
    public void runJob(){
 
        if (!auditService.getTodaysJobStatusByJobName(String.valueOf(JobName.DSP_TICKER)).isEmpty()) return;        
        long jobStart = System.currentTimeMillis();
        int count = 0;
        
        JobStatus audit = auditService.startJob(JobName.DSP_TICKER);
        log.info("DSP job started: {}", audit);

        List<Stock> stocks = stockService.getActiveTickers();
        Map<String, LoadDspTickers> processed = new HashMap<>();

        for (Stock stock : stocks) {
            long tickerStart = System.currentTimeMillis();

            try {
                Bot dspBot = SerializationUtils.clone(DSP_BOT);
                Map<String,String> gParam = Map.of("$gParam1$",stock.getTicker());
                BotUtil botUtil = new BotUtil(dspBot, gParam);
                String data = botUtil.process();

                LoadDspTickers loadDspTickers = new LoadDspTickers();
                loadDspTickers.setTicker(stock.getTicker());
                loadDspTickers.setCompanyDetails(data);                
                processed.put(stock.getTicker(), loadDspTickers);
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




}

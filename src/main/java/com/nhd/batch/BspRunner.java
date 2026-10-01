package com.nhd.batch;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhd.models.JobStatus;
import com.nhd.models.LoadBspTicker;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
public class BspRunner {

    private static final Logger log = LoggerFactory.getLogger(BspRunner.class);

    private static final Bot BSP_BOT;
    private static Integer STOCKS_PER_RUN = 2;

    static {        
        Yaml yaml = new Yaml(new Constructor(Bot.class, new LoaderOptions()));
        InputStream inputStream = BspRunner.class.getClassLoader().getResourceAsStream("yamlConfigs/bsp.yaml");
         BSP_BOT = yaml.load(inputStream);
        log.info(BSP_BOT.toString());       
    }

    @Autowired
    private StockService stockService ;

    @Autowired
    private AuditService auditService ;

    @Scheduled(cron="#{${loader.bsp_ticker.scheduler.cron}}")
    public void runJob(){

        List<Stock> remainingStocks = stockService.noHistoryStocksWithLimit(STOCKS_PER_RUN);
        if (remainingStocks.isEmpty()){
            log.info("BSP: No tickers available stopping...");
            return;
        }

        List<String> processedTickers = new ArrayList<>();
        JobStatus audit = auditService.startJob(JobName.BSP_TICKER);

        log.info( "BSP Loader  Start: Total = {},  Retrieved = {}", remainingStocks.size(), processedTickers.size());

        remainingStocks.forEach(item -> {
            JobStatus bspUnit = auditService.startJobWithComment(JobName.BSP_TICKER_UNIT, item.getTicker());
            try{

                Bot bot = runJob0(item);
                List<LoadBspTicker> loadBspTickers = buildObjects(bot);

                item.setHistoryLoaded(true);
                stockService.saveAllLoadBspTickers(loadBspTickers);
                stockService.saveStock(item);
                processedTickers.add(item.getTicker());
                auditService.endJobWithSuccessFailureCount(bspUnit,loadBspTickers.size(),0);

            } catch (PageLoadException | IOException | InterruptedException  e) {
                log.error(null, e);
                auditService.failJobWithSuccessFailureCount(bspUnit, 0, 0);
            }
        });
    
        log.info( "BSP Loader End: Total = {},  Retrieved = {}", remainingStocks.size(), processedTickers.size());
        auditService.endJobWithSuccessFailureCount(audit,processedTickers.size(),(remainingStocks.size()-processedTickers.size()));
    }

    public List<LoadBspTicker> buildObjects(Bot bot) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<LoadBspTicker> allLoadBspTickers = new ArrayList<>();

        for (Page page : bot.getPages()) {
            if(page.getPersistantData()){
                List<LoadBspTicker> loadBspTickers = mapper.readValue(page.getResponseData(), new TypeReference<List<LoadBspTicker>>() {});
                allLoadBspTickers.addAll(loadBspTickers);
            }
        }
        
        return allLoadBspTickers;
    }

    public Bot runJob0(Stock stock) throws PageLoadException, IOException, InterruptedException{
        Bot bspBot = SerializationUtils.clone(BSP_BOT);
        Map<String,String> gParam = Map.of("$gParam1$",stock.getTicker());
        BotUtil botUtil = new BotUtil(bspBot, gParam);
        Page templatePage = bspBot.getPages().remove(bspBot.getPages().size()-1);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(stock.getDateOfListing());
        List<Integer> years = getYears(cal.get(java.util.Calendar.YEAR));
        
        for (Integer eachYear : years) {
            Page page = SerializationUtils.clone(templatePage);
            page.setUrl(page.getUrl().replace("$startDate1$","01-01-"+eachYear));
            page.setUrl(page.getUrl().replace("$endDate1$","31-12-"+eachYear));
            bspBot.getPages().add(page);
        }

        botUtil.process();
        return bspBot;
    }

    public List<Integer> getYears(int startYear) {
        List<Integer> availableYears = new ArrayList<>();        
        Calendar calendar = new GregorianCalendar();
        int endYear = calendar.get(Calendar.YEAR);        
        for (int i = startYear; i <= endYear; i++) {
            availableYears.add(i);
        }        
        return availableYears;
    }

}

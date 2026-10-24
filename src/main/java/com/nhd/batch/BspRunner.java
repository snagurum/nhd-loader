package com.nhd.batch;


import com.nhd.models.JobStatus;
import com.nhd.models.LoadBspTicker;
import com.nhd.models.Stock;
import com.nhd.service.AuditService;
import com.nhd.service.BspService;
import com.nhd.service.StockDateProjection;
import com.nhd.service.StockService;
import com.nhd.util.DateSplitter;
import com.nhd.util.DateUtil;
import com.nhd.util.JobName;
import com.nhd.util.DateSplitter.DatePair;
import com.web.crawler.Bot;
import com.web.crawler.BotUtil;
import com.web.crawler.Page;
import com.web.crawler.PageLoadException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@AllArgsConstructor
public class BspRunner {

    private static final Logger log = LoggerFactory.getLogger(BspRunner.class);

    private static final Bot BSP_BOT;
    private static Integer STOCKS_PER_RUN = 20;

    static {        
        Yaml yaml = new Yaml(new Constructor(Bot.class, new LoaderOptions()));
        InputStream inputStream = BspRunner.class.getClassLoader().getResourceAsStream("yamlConfigs/bsp.yaml");
         BSP_BOT = yaml.load(inputStream);
        log.info(BSP_BOT.toString());       
    }

    private BspService bspService ;

    private StockService stockService ;

    private AuditService auditService ;

    @Scheduled(cron="#{${loader.bsp_ticker.scheduler.cron}}")
    public void runJob(){

        List<StockDateProjection> remainingStockDateProjections = bspService.getBspLoadPendingStockDatesWithLimit(STOCKS_PER_RUN);
        if (remainingStockDateProjections.isEmpty()){
            log.info("BSP: No tickers available stopping...");
            return;
        }

        List<String> processedTickers = new ArrayList<>();
        JobStatus audit = auditService.startJob(JobName.BSP_TICKER);

        log.info( "BSP Loader  Start: Total = {},  Retrieved = {}", remainingStockDateProjections.size(), processedTickers.size());

        remainingStockDateProjections.forEach(item -> {
            JobStatus bspUnit = auditService.startJobWithComment(JobName.BSP_TICKER_UNIT, item.getTicker());
            try{

                Bot bot = runJob0(item);
                List<LoadBspTicker> loadBspTickers = buildObjects(bot);
                // item.setHistoryLoaded(true);
                persistData(loadBspTickers);
                processedTickers.add(item.getTicker());
                auditService.endJobWithSuccessFailureCount(bspUnit,loadBspTickers.size(),0);

            } catch (PageLoadException | IOException | InterruptedException  e) {
                log.error(null, e);
                auditService.failJobWithSuccessFailureCount(bspUnit, 0, 1);
            }
        });
    
        log.info( "BSP Loader End: Total = {},  Retrieved = {}", remainingStockDateProjections.size(), processedTickers.size());
        auditService.endJobWithSuccessFailureCount(audit,processedTickers.size(),(remainingStockDateProjections.size()-processedTickers.size()));
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

    public Bot runJob0(StockDateProjection stockDateProjection) throws PageLoadException, IOException, InterruptedException{
        Bot bspBot = SerializationUtils.clone(BSP_BOT);
        Map<String,String> gParam = Map.of("$gParam1$",stockDateProjection.getTicker());
        BotUtil botUtil = new BotUtil(bspBot, gParam);
        Page templatePage = bspBot.getPages().remove(bspBot.getPages().size()-1);

        LocalDate endDate = LocalDate.now();
        List<DatePair> datePairs = DateSplitter.splitByYear(stockDateProjection.getLatestDate(), endDate);
        for(DatePair datePair: datePairs){
            Page page = SerializationUtils.clone(templatePage);
            page.setUrl(page.getUrl().replace("$startDate1$",DateUtil.getDateStr_ddMMyyyy(datePair.startDate())));
            page.setUrl(page.getUrl().replace("$endDate1$",DateUtil.getDateStr_ddMMyyyy(datePair.endDate())));
            bspBot.getPages().add(page);
        }

        botUtil.process();
        return bspBot;
    }

    public void persistData(List <LoadBspTicker> loadBspTickers, Stock stock){
        bspService.saveAllLoadBspTickers(loadBspTickers);
        stockService.saveStock(stock);
    }

    public void persistData(List <LoadBspTicker> loadBspTickers){
        bspService.saveAllLoadBspTickers(loadBspTickers);
    }

}

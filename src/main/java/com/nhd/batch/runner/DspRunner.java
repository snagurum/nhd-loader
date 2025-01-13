package com.nhd.batch.runner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nhd.models.HttpResponse;
import com.nhd.models.JobStatus;
import com.nhd.models.LoadDspTickers;
import com.nhd.models.Stock;
import com.nhd.service.AuditService;
import com.nhd.service.StockService;
import com.nhd.util.Constants;
import com.nhd.util.JobName;
import com.nhd.util.http.CookieHandler;
import com.nhd.util.http.Http;

@Component
public class DspRunner {

    private static final Logger log = LoggerFactory.getLogger(DspRunner.class);

    private Boolean jobCompleted4Today = false;

    @Autowired
    private StockService stockService ;

    @Autowired
    private AuditService auditService ;

    private Integer totalRounds = 5;

    public void homePage(Stock ticker, CookieHandler cookies) throws IOException{
        log.debug(" ====>>>> homePage");
        Http.loadPage(Constants.NSE_HOME_URL, cookies);
    }

    public void tickerPage(Stock ticker, CookieHandler cookies) throws IOException{
        log.debug(" ====>>>> tickerPage");
        Http.loadPage(
            Constants.NSE_HOME_URL+"/get-quotes/equity?symbol=" + ticker.getTicker()
            , cookies);
    }

    public HttpResponse tickerDetails(Stock ticker, CookieHandler cookies) throws IOException{
        log.debug(" ====>>>> tickerDetails");
        return Http.loadPage(
            Constants.NSE_HOME_URL+"/api/quote-equity?symbol=" + ticker.getTicker()
            , cookies);
    }

    public HttpResponse tickerTradeDetails(Stock ticker, CookieHandler cookies) throws IOException{
        log.debug(" ====>>>> tickerTradeDetails");
        return Http.loadPage(
            Constants.NSE_HOME_URL+"/api/quote-equity?symbol="+ ticker.getTicker() + "&section=trade_info"
            , cookies);
    }

    public LoadDspTickers getDspTickerInfo(Stock ticker){
        try{
            CookieHandler cookies = new CookieHandler() ;
            homePage(ticker, cookies);
            tickerPage(ticker, cookies);
            String companyDetails = tickerDetails(ticker, cookies).getResponseBody();
            String tradeDetails = tickerTradeDetails(ticker, cookies).getResponseBody();
            LoadDspTickers loadDspTickers = new LoadDspTickers();
            loadDspTickers.setTicker(ticker.getTicker());
            loadDspTickers.setCompanyDetails(companyDetails);
            loadDspTickers.setTradeDetails(tradeDetails);
            return loadDspTickers;
        }catch(Exception e){
            log.error(ticker.getTicker(),e.getMessage(), e);
            return null;
        }

    }

    @Scheduled(cron="#{${loader.dsp_ticker.scheduler.cron}}")
    public void runJob(){
        List<JobStatus> jobs = auditService.getTodaysJobStatusByJobName(String.valueOf(JobName.DSP_TICKER));
        if(jobs.isEmpty()) {
            jobCompleted4Today = false; 
        }else {
            return;
        }
        JobStatus audit = auditService.startJob(JobName.DSP_TICKER);
            log.info("Job {} has already been started ....",audit);
        
        List<Stock> remainingStocks = stockService.getActiveTickers();
        Map<String, LoadDspTickers> processedTickers = new HashMap<>();

        int rounds = 0;
        boolean allFailed = false;
        while( rounds < totalRounds && !allFailed && !remainingStocks.isEmpty()) {
            log.info( "DSP Loader Start: Round = {}, Remaining = {},  Retrieved = {}", rounds,remainingStocks.size(), processedTickers.keySet().size());
            AtomicInteger failureCount = new AtomicInteger();
            remainingStocks.stream().parallel().forEach(item -> {
                LoadDspTickers dspTicker = getDspTickerInfo(item);
                if (dspTicker != null) {
                    processedTickers.put(item.getTicker(), dspTicker);
                    if(processedTickers.size()%100 == 0){
                        log.info("DSP ticker processed count = {}",processedTickers.size());
                    }
                }
                else {
                    failureCount.getAndIncrement();
                }
            });
            List<String> tickerNames0 = processedTickers.keySet().stream().toList();
            remainingStocks.removeIf(stock -> tickerNames0.contains(stock.getTicker()));
            log.info( "DSP Loader End: Round = {}, Remaining = {},  Retrieved = {}", rounds,remainingStocks.size(), processedTickers.keySet().size());
            rounds++;
        }

        stockService.saveAllLoadDspTickers(processedTickers.values().stream().toList());
        auditService.endJobWithSuccessFailureCount(audit,processedTickers.keySet().size(),remainingStocks.size());
        jobCompleted4Today = true;

    }


}

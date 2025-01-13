package com.nhd.batch.runner;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.nhd.models.HttpResponse;
import com.nhd.models.JobStatus;
import com.nhd.models.LoadBulkTickers;
import com.nhd.models.Stock;
import com.nhd.service.AuditService;
import com.nhd.service.StockService;
import com.nhd.util.Constants;
import com.nhd.util.JobName;
import com.nhd.util.http.CookieHandler;
import com.nhd.util.http.Http;


@Component
public class BulkRunner {
    

    private static final Logger log = LoggerFactory.getLogger(BulkRunner.class);

    @Autowired
    private StockService stockService ;

    @Autowired
    private AuditService auditService ;

    private static Map<String,String> additionalHeaders = new HashMap<>();
    
    private static String homePageUrl = Constants.NSE_HOME_URL;
    private static String tickerPageUrl = Constants.NSE_HOME_URL+"/get-quotes/equity?symbol=";
    private static String tickerDetailsPageUrl = Constants.NSE_HOME_URL+"/api/quote-equity?symbol=";
    private static String tickerHistoricalPriceDetailsUrl = Constants.NSE_HOME_URL+"/api/historical/cm/equity?symbol=__ticker__&series=[%22__series__%22]&from=__startDate__&to=__endDate__&csv=true";

    private static Integer totalRounds = 5;
    private static Integer STOCKS_PER_RUN = 50;

    static{
        additionalHeaders.put("Referer",homePageUrl+"/");
        additionalHeaders.put("Connection","keep-alive");
        additionalHeaders.put("Upgrade-Insecure-Requests","1");
        additionalHeaders.put("Sec-Fetch-Dest","document");
        additionalHeaders.put("Sec-Fetch-Mode","navigate");
        additionalHeaders.put("Sec-Fetch-Site","same-origin");
        additionalHeaders.put("Sec-Fetch-User","?1");
        additionalHeaders.put("TE","trailers");

    }
    
    public void homePage(Stock ticker, CookieHandler cookies) throws IOException{
        log.debug("homePage {}",ticker.getTicker());
        Http.loadPage(homePageUrl, cookies);
    }

    public void tickerPage(Stock ticker, CookieHandler cookies) throws IOException{
        log.debug("tickerPage {}",ticker.getTicker());
        Http.loadPage(
            tickerPageUrl + URLEncoder.encode(ticker.getTicker(),"UTF-8")
            , cookies);
    }

    public HttpResponse tickerDetails(Stock ticker, CookieHandler cookies) throws IOException{
        log.debug("tickerDetails {}",ticker.getTicker());
        return Http.loadPage(
            tickerDetailsPageUrl + URLEncoder.encode(ticker.getTicker(),"UTF-8")
            , cookies);
    }

    
    public HttpResponse tickerHistoricalPriceDetails(Stock ticker, CookieHandler cookies,String startDate, String endDate) throws IOException{
        log.debug("tickerHistoricalPriceDetails {} start: url={}",ticker.getTicker(),                tickerHistoricalPriceDetailsUrl
                .replaceFirst("__ticker__", ticker.getTicker())
                .replaceFirst("__startDate__", startDate)
                .replaceFirst("__endDate__", endDate));
        HttpResponse httpResponse =Http.loadPage(
                tickerHistoricalPriceDetailsUrl
                        .replaceFirst("__ticker__", URLEncoder.encode(ticker.getTicker(),"UTF-8"))
                        .replaceFirst("__series__", ticker.getSeries())
                        .replaceFirst("__startDate__", startDate)
                        .replaceFirst("__endDate__", endDate)
                , cookies,additionalHeaders);
        log.debug("tickerHistoricalPriceDetails {} response {} end",ticker.getTicker(),httpResponse);
        return httpResponse;
    }


    public List<LoadBulkTickers> getYearlyBulkTickerData(Stock ticker,List<String> datesList){
        try{
            CookieHandler cookies = new CookieHandler() ;
            List<LoadBulkTickers> loadBulkTickersList = new ArrayList<>();
            homePage(ticker, cookies);
            tickerPage(ticker, cookies);
            AtomicReference<String> data = new AtomicReference<>();

            datesList.forEach(item -> {
                String startDate = item.split("__")[0];
                String endDate = item.split("__")[1];
                try {
                    String dataString = tickerHistoricalPriceDetails(ticker, cookies,startDate,endDate).getResponseBody() ;
                    if(dataString==null || dataString.isEmpty()) {
                        data.set("Error");
                        log.info("No data retrieved for {} with dates {} ,{}", ticker.getTicker(), startDate, endDate);
                    }else
                        loadBulkTickersList.addAll(this.loadObjectList( dataString));
                } catch (IOException e) {
                    data.set("Error");
                    log.error("Exception occured for {} with dates {} ,{}", ticker.getTicker(), startDate, endDate,e);
                }
            });
            if("Error".equals(data.get()))
                return null;
            else return loadBulkTickersList;
        }catch(IOException e){
            log.error("Exception occured for {} ", ticker.getTicker(),e);
            return null;
        }

    }


    public List<String> getDates(Stock ticker){
        List<String> availableDates = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(ticker.getDateOfListing());
        int startYear = calendar.get(Calendar.YEAR);
        Date endDate = new Date();
        calendar.setTime(endDate);
        int endYear = calendar.get(Calendar.YEAR);

        for(int i=startYear;i<=endYear;i++){
            availableDates.add("01-01-"+i+"__31-12-"+i);
        }
        return availableDates;
    }

    public List<LoadBulkTickers> getAllBulkTickerData(Stock ticker){
        try{
            List<String> datesList = this.getDates(ticker);
            return getYearlyBulkTickerData(ticker, datesList);
        }catch(Exception e){
            return null;
        }

    }


    @Scheduled(cron="#{${loader.bulk_ticker.scheduler.cron}}")
    public void runJob(){

        List<Stock> remainingStocks = stockService.noHistoryStocksWithLimit(STOCKS_PER_RUN);
        if (remainingStocks.isEmpty()){
            log.info("BSP: No tickers available stopping...");
            return;
        }

        List<String> processedTickers = new ArrayList<>();

        int rounds = 0;
        boolean allFailed = false;
        JobStatus audit = auditService.startJob(JobName.BSP_TICKER);

        while( rounds < totalRounds && !allFailed && !remainingStocks.isEmpty()) {
            log.info( "BSP Loader  Start: Round-{}, Remaining = {},  Retrieved = {}", rounds,remainingStocks.size(), processedTickers.size());
            AtomicInteger failureCount = new AtomicInteger();
            remainingStocks.parallelStream().forEach(item -> {
                JobStatus bspUnit = auditService.startJobWithComment(JobName.BSP_TICKER_UNIT, item.getTicker());
                List<LoadBulkTickers> bulkTickerList = getAllBulkTickerData(item);
                if(bulkTickerList == null){
                    failureCount.getAndIncrement();
                    auditService.failJob(bspUnit);            
                    log.info("Error occured for {}, no data retrieved bulkTickerList is empty", item.getTicker());
                }else{
                    bulkTickerList.forEach(i -> i.setTicker(item.getTicker()));
                    if (!bulkTickerList.isEmpty()) {
                        item.setHistoryLoaded(true);
                        stockService.saveAllLoadBulkTickers(bulkTickerList);
                        stockService.saveStock(item);
                        processedTickers.add(item.getTicker());
                        auditService.endJobWithSuccessFailureCount(bspUnit,bulkTickerList.size(),0);
                    }else {
                        failureCount.getAndIncrement();
                        auditService.endJobWithSuccessFailureCount(bspUnit,0,0);
                    }
                }
            });
            remainingStocks.removeIf(stock -> processedTickers.contains(stock.getTicker()));
            log.info( "BSP Loader End: Round = {}, Remaining = {},  Retrieved = {}", rounds,remainingStocks.size(), processedTickers.size());
            rounds++;
        }

        auditService.endJobWithSuccessFailureCount(audit,processedTickers.size(),remainingStocks.size());
    }


    public List<LoadBulkTickers> loadObjectList(String dataString) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.builder()
                .addColumn("Date")
                .addColumn("series")
                .addColumn("OPEN")
                .addColumn("HIGH")
                .addColumn("LOW")
                .addColumn("PREV. CLOSE")
                .addColumn("ltp")
                .addColumn("close")
                .addColumn("vwap")
                .addColumn("52W H")
                .addColumn("52W L")
                .addColumn("VOLUME")
                .addColumn("VALUE")
                .addColumn("No of trades")
                .build()
                .withHeader();
            MappingIterator<LoadBulkTickers> it = csvMapper.readerFor(LoadBulkTickers.class).with(schema).readValues(dataString);
            return it.readAll();
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file {}", dataString, e);
            return Collections.emptyList();
        }
    }
}

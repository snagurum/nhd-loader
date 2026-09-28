package com.nhd.batch.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    private StockService stockService;

    @Autowired
    private AuditService auditService;

    private Integer totalRounds = 5;

    public void homePage(Stock ticker, CookieHandler cookies) throws IOException {
        log.debug(" ====>>>> homePage");
        Http.loadPage(Constants.NSE_HOME_URL, cookies);
    }

    public void tickerPage(Stock ticker, CookieHandler cookies) throws IOException {
        log.debug(" ====>>>> tickerPage");
        Http.loadPage(
                Constants.NSE_HOME_URL + "/get-quotes/equity?symbol=" + ticker.getTicker(), cookies);
    }

    public HttpResponse tickerDetails(Stock ticker, CookieHandler cookies) throws IOException {
        log.debug(" ====>>>> tickerDetails");
        return Http.loadPage(
                Constants.NSE_HOME_URL + "/api/quote-equity?symbol=" + ticker.getTicker(), cookies);
    }

    public HttpResponse tickerTradeDetails(Stock ticker, CookieHandler cookies) throws IOException {
        log.debug(" ====>>>> tickerTradeDetails");
        return Http.loadPage(
                Constants.NSE_HOME_URL + "/api/quote-equity?symbol=" + ticker.getTicker() + "&section=trade_info",
                cookies);
    }

    public LoadDspTickers getDspTickerInfo(Stock ticker) {
        try {
            CookieHandler cookies = new CookieHandler();
            homePage(ticker, cookies);
            tickerPage(ticker, cookies);
            String companyDetails = tickerDetails(ticker, cookies).getResponseBody();
            String tradeDetails = tickerTradeDetails(ticker, cookies).getResponseBody();
            LoadDspTickers loadDspTickers = new LoadDspTickers();
            loadDspTickers.setTicker(ticker.getTicker());
            loadDspTickers.setCompanyDetails(companyDetails);
            loadDspTickers.setTradeDetails(tradeDetails);
            return loadDspTickers;
        } catch (Exception e) {
            log.error(ticker.getTicker(), e.getMessage(), e);
            return null;
        }

    }
    /*
     * @Scheduled(cron="#{${loader.dsp_ticker.scheduler.cron}}")
     * public void runJob(){
     * List<JobStatus> jobs =
     * auditService.getTodaysJobStatusByJobName(String.valueOf(JobName.DSP_TICKER));
     * if(jobs.isEmpty()) {
     * jobCompleted4Today = false;
     * }else {
     * return;
     * }
     * JobStatus audit = auditService.startJob(JobName.DSP_TICKER);
     * log.info("Job {} has already been started ....",audit);
     * 
     * List<Stock> remainingStocks = stockService.getActiveTickers();
     * Map<String, LoadDspTickers> processedTickers = new HashMap<>();
     * 
     * int rounds = 0;
     * boolean allFailed = false;
     * while( rounds < totalRounds && !allFailed && !remainingStocks.isEmpty()) {
     * log.info( "DSP Loader Start: Round = {}, Remaining = {},  Retrieved = {}",
     * rounds,remainingStocks.size(), processedTickers.keySet().size());
     * AtomicInteger failureCount = new AtomicInteger();
     * remainingStocks.stream().parallel().forEach(item -> {
     * LoadDspTickers dspTicker = getDspTickerInfo(item);
     * if (dspTicker != null) {
     * processedTickers.put(item.getTicker(), dspTicker);
     * if(processedTickers.size()%100 == 0){
     * log.info("DSP ticker processed count = {}",processedTickers.size());
     * }
     * }
     * else {
     * failureCount.getAndIncrement();
     * }
     * });
     * List<String> tickerNames0 = processedTickers.keySet().stream().toList();
     * remainingStocks.removeIf(stock -> tickerNames0.contains(stock.getTicker()));
     * log.info( "DSP Loader End: Round = {}, Remaining = {},  Retrieved = {}",
     * rounds,remainingStocks.size(), processedTickers.keySet().size());
     * rounds++;
     * }
     * 
     * stockService.saveAllLoadDspTickers(processedTickers.values().stream().toList(
     * ));
     * auditService.endJobWithSuccessFailureCount(audit,processedTickers.keySet().
     * size(),remainingStocks.size());
     * jobCompleted4Today = true;
     * 
     * }
     */

/*    
    @Scheduled(cron="#{${loader.dsp_ticker.scheduler.cron}}")
//    @Scheduled(cron = "${loader.dsp_ticker.scheduler.cron}")
    public void runJob() {

        List<JobStatus> jobs = auditService.getTodaysJobStatusByJobName(String.valueOf(JobName.DSP_TICKER));

        if (!jobs.isEmpty()) {
            return;
        }

        jobCompleted4Today = false;
        JobStatus audit = auditService.startJob(JobName.DSP_TICKER);
        log.info("DSP job started: {}", audit);

        List<Stock> remainingStocks = new ArrayList<>(stockService.getActiveTickers());
        Map<String, LoadDspTickers> processedTickers = new ConcurrentHashMap<>();

        int rounds = 0;
        int maxRounds = totalRounds;

        // Bounded executor (VERY IMPORTANT)
        int threads = 5; // keep small for NSE
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Rate limiter
        Semaphore rateLimiter = new Semaphore(3);

        try {
            while (rounds < maxRounds && !remainingStocks.isEmpty()) {

                log.info(
                        "DSP Loader Start: Round={}, Remaining={}, Processed={}",
                        rounds,
                        remainingStocks.size(),
                        processedTickers.size());

                List<Future<?>> futures;
                futures = new ArrayList<>();

                for (Stock stock : remainingStocks) {
                    futures.add(executor.submit(() -> {
                        try {
                            rateLimiter.acquire();
                            LoadDspTickers dspTicker = getDspTickerInfo(stock);
                            if (dspTicker != null) {
                                processedTickers.put(stock.getTicker(), dspTicker);
                                int count = processedTickers.size();
                                if (count % 100 == 0) {
                                    log.info("DSP ticker processed count={}", count);
                                }
                            }

                        } catch (Exception e) {
                            log.warn("DSP ticker failed: {}", stock.getTicker(), e);
                        } finally {
                            rateLimiter.release();
                        }
                    }));
                }

                // Wait for completion with timeout
                for (Future<?> future : futures) {
                    try {
                        future.get(20, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        future.cancel(true);
                    } catch (Exception ignored) {
                    }
                }

                // Remove processed stocks
                Set<String> processed = processedTickers.keySet();
                remainingStocks.removeIf(s -> processed.contains(s.getTicker()));

                log.info(
                        "DSP Loader End: Round={}, Remaining={}, Processed={}",
                        rounds,
                        remainingStocks.size(),
                        processedTickers.size());
                rounds++;
            }

        } finally {
            executor.shutdownNow();
        }

        stockService.saveAllLoadDspTickers(new ArrayList<>(processedTickers.values()));
        auditService.endJobWithSuccessFailureCount(audit, processedTickers.size(), remainingStocks.size());
        jobCompleted4Today = true;
    }
 */

@Scheduled(cron="#{${loader.dsp_ticker.scheduler.cron}}")
// @Scheduled(cron = "${loader.dsp_ticker.scheduler.cron}")
public void runJob() {

    if (!auditService
            .getTodaysJobStatusByJobName(String.valueOf(JobName.DSP_TICKER))
            .isEmpty()) {
        return;
    }

    JobStatus audit = auditService.startJob(JobName.DSP_TICKER);
    log.info("DSP job started: {}", audit);

    List<Stock> stocks = stockService.getActiveTickers();
    Map<String, LoadDspTickers> processed = new HashMap<>();

    long jobStart = System.currentTimeMillis();
    int count = 0;

    for (Stock stock : stocks) {
        long tickerStart = System.currentTimeMillis();

        try {
            LoadDspTickers dsp = getDspTickerInfo(stock);

            if (dsp != null) {
                processed.put(stock.getTicker(), dsp);
            }

        } catch (Exception e) {
            log.warn("DSP ticker failed: {}", stock.getTicker(), e);
        }

        long tickerTime = System.currentTimeMillis() - tickerStart;
        count++;

        // 🕒 PER-TICKER LOG (this is what you asked for)
        log.info(
            "DSP ticker={} took {} ms (#{})",
            stock.getTicker(),
            tickerTime,
            count
        );

        // 🔴 THROTTLE (critical for NSE)
        sleepQuietly(300);

        // 📊 PERIODIC STATS
        if (count % 100 == 0) {
            long elapsed = System.currentTimeMillis() - jobStart;
            long avg = elapsed / count;

            log.info(
                "DSP progress: {} tickers, elapsed={} sec, avg={} ms/ticker",
                count,
                elapsed / 1000,
                avg
            );
        }
    }

    stockService.saveAllLoadDspTickers(new ArrayList<>(processed.values()));

    auditService.endJobWithSuccessFailureCount(
        audit,
        processed.size(),
        stocks.size() - processed.size()
    );

    long totalTime = System.currentTimeMillis() - jobStart;
    log.info(
        "DSP job completed: total={} sec, avg={} ms/ticker",
        totalTime / 1000,
        totalTime / Math.max(1, stocks.size())
    );
}

private void sleepQuietly(int i) {
    try {
        Thread.sleep(i);
    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}



}

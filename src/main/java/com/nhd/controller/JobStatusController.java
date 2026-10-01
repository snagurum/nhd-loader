package com.nhd.controller;


// import com.nhd.batch.runner.BulkRunner;
// import com.nhd.batch.runner.DspRunner;
// import com.nhd.batch.runner.TickerRunner;
import com.nhd.batch.BspRunner;
import com.nhd.batch.DspRunner;
import com.nhd.batch.TickerRunner;
import com.nhd.models.JobStatus;
import com.nhd.models.LoadBspTicker;
import com.nhd.models.LoadDspTicker;
import com.nhd.models.Stock;
import com.nhd.service.AuditService;
import com.web.crawler.Bot;
import com.web.crawler.BotUtil;
import com.web.crawler.PageLoadException;

import lombok.AllArgsConstructor;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor 
public class JobStatusController {

	private static final Logger log = LoggerFactory.getLogger(JobStatusController.class);

	// @Autowired
	DspRunner dspTickerRunner;

	// @Autowired
	BspRunner bspTickerRunner;

	// @Autowired
	TickerRunner tickerRunner;

	// @Autowired
	AuditService auditService;

	@GetMapping("/tjs")
	public List<JobStatus> index() {
		return auditService.getTodaysJobStatus();
	}

	@GetMapping("/help")
	public String help(){
		StringBuilder message = new StringBuilder();
		message
                        .append("/help                                               help message" ).append("\n")
                        .append("/tjs                                                Today's Job Status ").append("\n")
                        .append("/runJob/ticker                                      Run Ticker job").append("\n")
                        .append("/runJob/dspTicker                                   Run DSP Ticker job").append("\n")
                        .append("/runJob/bspTicker                                   Run BSP Ticker job").append("\n")
                        .append("/getData/bspTicker?ticker=INFY&fromDate=2024-11-18  GET BSP Ticker Data").append("\n")
                        .append("/getData/dspTicker?ticker=INFY&fromDate=2024-11-18  GET DSP Ticker Data").append("\n")
                        .append("/test/bulk?ticker=INFY&dol=2024-11-18&series=BE     test BSP Ticker job").append("\n")
                        .append("/test/dsp?ticker=INFY                               test DSP Ticker job").append("\n")
		;
		return message.toString();
	}

	@GetMapping("/runJob/ticker")
	public void runTickerJob(){
		tickerRunner.runJob();
	}

	@GetMapping("/runJob/dspTicker")
	public void runDspTickerJob(){
		dspTickerRunner.runJob();
	}

	@GetMapping("/runJob/bspTicker")
	public void runBulkTickerJob(){
		bspTickerRunner.runJob();
	}

	@GetMapping("/test/dsp")
	public LoadDspTicker dspTest(@RequestParam Optional<String> ticker) throws PageLoadException, IOException, InterruptedException {
		Stock stock = new Stock();
		if(!ticker.isPresent()) {
			stock.setTicker("INFY");
		}else{
			stock.setTicker(ticker.get());
		}
		Bot DSP_BOT;
                Yaml yaml = new Yaml(new Constructor(Bot.class, new LoaderOptions()));
                InputStream inputStream = DspRunner.class.getClassLoader().getResourceAsStream("yamlConfigs/dsp.yaml");
                DSP_BOT = yaml.load(inputStream);
                System.out.println(DSP_BOT);   

		Bot dspBot = SerializationUtils.clone(DSP_BOT);
		Map<String, String> gParam = new HashMap<>(Map.of("$gParam1$", stock.getTicker()));
		BotUtil botUtil = new BotUtil(dspBot, gParam);
		botUtil.process();
		LoadDspTicker loadDspTickers = new LoadDspTicker();
		loadDspTickers.setTicker(stock.getTicker());
		loadDspTickers.setCompanyDetails(dspBot.getPages().get(2).getResponseData()); 

		return loadDspTickers;
	}


	@GetMapping("/test/dsp1")
	public void dspTest1(@RequestParam Optional<String> ticker)  {
	 try {
            // 1. Setup a persistent CookieManager to store the NSE session cookies
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            // 2. Build the HttpClient with the Cookie Handler
            HttpClient client = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Define standard browser headers required to bypass NSE's scraper detection
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8";
            String acceptLang = "en-US,en;q=0.9";
            String acceptEncoding = "identity";

            // ==========================================
            // STEP 1: Hit the Home Page to fetch cookies
            // ==========================================
            System.out.println("Step 1: Initializing session via landing page...");
            HttpRequest homeRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.nseindia.com"))
                    .header("User-Agent", userAgent)
                    .header("Accept", accept)
                    .header("Accept-Language", acceptLang)
                    .header("Accept-Encoding", acceptEncoding)
                    .GET()
                    .build();

            HttpResponse<String> homeResponse = client.send(homeRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Home Page Response Code: " + homeResponse.statusCode());
			System.out.println("Quote API Status Code: " + homeResponse.body());

            // Add a brief pause to mimic human browser latency behavior
            Thread.sleep(1000);

            // ==========================================
            // STEP 2: Request Global Search Equity Data
            // ==========================================
            System.out.println("\nStep 2: Fetching global search data for INFY...");
            HttpRequest searchRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.nseindia.com/api/NextApi/globalSearch/equity?symbol=INFY"))
                    .header("User-Agent", userAgent)
                    .header("Accept", accept)
                    .header("Accept-Language", acceptLang)
                    .header("Accept-Encoding", acceptEncoding)
                    .header("Referer", "https://www.nseindia.com") // Critical referer context
                    .GET()
                    .build();

            HttpResponse<String> searchResponse = client.send(searchRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Search API Status Code: " + searchResponse.statusCode());
            System.out.println("Search API Body Preview:\n" + searchResponse.body());

            Thread.sleep(1000);

            // ==========================================
            // STEP 3: Request Detailed Quote Data
            // ==========================================
            System.out.println("\nStep 3: Fetching detailed Quote data for INFY...");
            HttpRequest quoteRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.nseindia.com//api/NextApi/apiClient/GetQuoteApi?functionName=getSymbolData&marketType=N&series=EQ&symbol=INFY"))
                    .header("User-Agent", userAgent)
                    .header("Accept", accept)
                    .header("Accept-Language", acceptLang)
                    .header("Accept-Encoding", acceptEncoding)
                    .header("Referer", "https://nseindia.com")
                    .GET()
                    .build();

            HttpResponse<String> quoteResponse = client.send(quoteRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Quote API Status Code: " + quoteResponse.statusCode());
            System.out.println("Quote API Body Preview:\n" + quoteResponse.body());
        } catch (Exception e) {
            System.err.println("An error occurred during execution:");
            e.printStackTrace();
        }
	}

	@GetMapping("/test/bsp")
	public String bulkTest(@RequestParam Optional<String> ticker, @RequestParam Optional<String> dol, @RequestParam Optional<String> series) throws PageLoadException, IOException, InterruptedException {
		Stock stock = new Stock();
		if(!ticker.isPresent()){
			 stock.setTicker("INFY");
			 stock.setSeries("EQ");
			 stock.setDateOfListing(Date.valueOf("1995-08-08"));
			// stock.setTicker("ZOMATO");
			// stock.setDateOfListing(Date.valueOf("2024-01-09"));
//			stock.setTicker("TATATECH");
//			stock.setDateOfListing(Date.valueOf("2023-01-09"));
		}
		else{
			stock.setTicker( ticker.get());
			stock.setDateOfListing(Date.valueOf(dol.get()));
			stock.setSeries(series.get());
			log.info("Ticker = {}, Dol = {}",stock.getTicker(),stock.getDateOfListing());
		}
                // String temp  = bspTickerRunner.runJobTemp("21STCENMGM", 1995);
                Bot bot  = bspTickerRunner.runJob0(stock);
                System.out.println( "data = "+ bot.getPages());
		return bot.getPages().toString();
	}

}
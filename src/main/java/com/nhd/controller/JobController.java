package com.nhd.controller;

import com.nhd.batch.BspRunner;
import com.nhd.batch.DspRunner;
import com.nhd.batch.TickerRunner;
import com.nhd.models.JobStatus;
import com.nhd.models.Stock;
import com.nhd.service.AuditService;
import com.web.crawler.Bot;
import com.web.crawler.BotUtil;
import com.web.crawler.PageLoadException;

import lombok.AllArgsConstructor;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@AllArgsConstructor 
public class JobController {

	private static final Logger log = LoggerFactory.getLogger(JobController.class);

	DspRunner dspTickerRunner;

	BspRunner bspTickerRunner;

	TickerRunner tickerRunner;

	AuditService auditService;

	@GetMapping("/tjs")
	public List<JobStatus> index() {
		return auditService.getTodaysJobStatus();
	}

	@GetMapping("/help")
	public String help(){
		StringBuilder message = new StringBuilder();
		message
			.append("/loader/help                                   help message" ).append("\n")
			.append("/loader/tjs                                    Today's Job Status ").append("\n")
			.append("/loader/runJob/ticker                          Run Ticker job").append("\n")
			.append("/loader/runJob/dspTicker                       Run DSP Ticker job").append("\n")
			.append("/loader/runJob/bspTicker                       Run BSP Ticker job").append("\n")
			.append("/loader/bsp?ticker=INFY&fromDate=2024-11-18    GET BSP Ticker Data").append("\n")
			.append("/loader/dsp?ticker=INFY&fromDate=2024-11-18    GET DSP Ticker Data").append("\n")
			.append("/loader/test/bsp?ticker=INFY&dol=2024-11-18    test BSP Ticker job").append("\n")
			.append("/loader/test/dsp?ticker=INFY                   test DSP Ticker job").append("\n")
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
	public void runBspTickerJob(){
		bspTickerRunner.runJob();
	}

	@GetMapping("/test/dsp")
	public String dspTest(@RequestParam Optional<String> ticker) throws PageLoadException, IOException, InterruptedException {
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

		Bot dspBot = SerializationUtils.clone(DSP_BOT);
		Map<String, String> gParam = new HashMap<>(Map.of("$gParam1$", stock.getTicker()));
		BotUtil botUtil = new BotUtil(dspBot, gParam);
		botUtil.process(); 

		return dspBot.getPages().get(2).getResponseData();
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
			// stock.setTicker("TATATECH");
			// stock.setDateOfListing(Date.valueOf("2023-01-09"));
		}
		else{
			stock.setTicker( ticker.get());
			stock.setDateOfListing(Date.valueOf(dol.get()));
			stock.setSeries(series.get());
			log.info("Ticker = {}, Dol = {}",stock.getTicker(),stock.getDateOfListing());
		}
		Bot bot  = bspTickerRunner.runJob0(stock);
		return bot.getPages().toString();
	}

}
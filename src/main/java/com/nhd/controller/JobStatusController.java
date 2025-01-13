package com.nhd.controller;


import com.nhd.batch.runner.BulkRunner;
import com.nhd.batch.runner.DspRunner;
import com.nhd.batch.runner.TickerRunner;
import com.nhd.models.JobStatus;
import com.nhd.models.LoadBulkTickers;
import com.nhd.models.LoadDspTickers;
import com.nhd.models.Stock;
import com.nhd.service.AuditService;
import com.nhd.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class JobStatusController {

	private static final Logger log = LoggerFactory.getLogger(JobStatusController.class);

	@Autowired
	DspRunner dspTickerRunner;

	@Autowired
	BulkRunner bulkTickerRunner;

	@Autowired
	TickerRunner tickerRunner;

	@Autowired
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

	@GetMapping("/runJob/bulkTicker")
	public void runBulkTickerJob(){
		bulkTickerRunner.runJob();
	}

	@GetMapping("/test/dsp")
	public LoadDspTickers dspTest(@RequestParam Optional<String> ticker) {
		Stock stock = new Stock();
		if(!ticker.isPresent()) {
			stock.setTicker("INFY");
		}else{
			stock.setTicker(ticker.get());
		}
		return dspTickerRunner.getDspTickerInfo(stock);
	}

	@GetMapping("/test/bulk")
	public List<LoadBulkTickers> bulkTest(@RequestParam Optional<String> ticker, @RequestParam Optional<String> dol, @RequestParam Optional<String> series) {
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
		return bulkTickerRunner.getAllBulkTickerData(stock);
	}

}
package com.nhd.batch.runner;


import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.nhd.models.JobStatus;
import com.nhd.models.LoadTickers;
import com.nhd.service.AuditService;
import com.nhd.service.StockService;
import com.nhd.util.Constants;
import com.nhd.util.JobName;
import com.nhd.util.http.Http;


@Component
public class TickerRunner {
    
    private static final Logger log = LoggerFactory.getLogger(TickerRunner.class);

    @Autowired
    private StockService stockService ;

    @Autowired
    private AuditService auditService ;

    private Boolean jobCompleted4Today = false;


	public String getTickers() throws IOException {
        return Http.loadPage(Constants.NSE_TICKERS_URL, null).getResponseBody();
	}

    @Scheduled( cron = "#{${loader.ticker.scheduler.cron}}")
    public void runJob(){
        try{
            List<JobStatus> jobs = auditService.getTodaysJobStatusByJobName(String.valueOf(JobName.TICKER));
            if(jobs.isEmpty()) {
               jobCompleted4Today = false; 
            }else {
                return;
            }
            JobStatus audit = auditService.startJob(JobName.TICKER);
            log.info("Job {} has already been started ....",audit);
            List<LoadTickers> tickers = this.loadObjectList(getTickers());
            stockService.saveAllLoadTickers(tickers );
            log.info("loaded tickers count = {}", tickers.size());
            audit.setSuccessCount(tickers.size());
            auditService.endJob(audit);
            jobCompleted4Today = true;
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    public List<LoadTickers> loadObjectList(String dataString) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.builder()
                .addColumn("SYMBOL")
                .addColumn("NAME OF COMPANY")
                .addColumn("SERIES")
                .addColumn("DATE OF LISTING")
                .addColumn("PAID UP VALUE")
                .addColumn("MARKET LOT")
                .addColumn("ISIN NUMBER")
                .addColumn("FACE VALUE")
                .build()
                .withHeader();
            MappingIterator<LoadTickers> it = csvMapper.readerFor(LoadTickers.class).with(schema).readValues(dataString);
            return it.readAll();
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file {}", dataString, e);
            return Collections.emptyList();
        }
    }


}

package com.nhd.controller;

import com.nhd.batch.runner.BulkRunner;
import com.nhd.models.LoadBulkTickers;
import com.nhd.models.LoadDspTickers;
import com.nhd.models.Stock;
import com.nhd.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/getData")
public class GetDataController {

    private static final Logger log = LoggerFactory.getLogger(GetDataController.class);

    @Autowired
    private StockService stockService ;

    @GetMapping("/stocks")
    public List<Stock> getStocks() {
        return stockService.getStocks();
    }

    @GetMapping("/bspTicker")
    public List<LoadBulkTickers> getBspTicker(@RequestParam String ticker, @RequestParam Optional<String> fromDate) {
        Date date = null;
        if(fromDate.isPresent())
            date = Date.valueOf(fromDate.get());
        return stockService.getBspTickers(ticker,date);
    }

    @GetMapping("/dspTicker")
    public List<LoadDspTickers> getDspTicker(@RequestParam String ticker, @RequestParam Optional<String> fromDate) {
        Date date = null;
        if(fromDate.isPresent())
            date = Date.valueOf(fromDate.get());
        return stockService.getDspTickers(ticker,date);
    }

}
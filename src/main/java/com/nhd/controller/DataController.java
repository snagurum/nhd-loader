package com.nhd.controller;

import com.nhd.models.LoadBspTicker;
import com.nhd.models.LoadDspTicker;
import com.nhd.models.Stock;
import com.nhd.service.StockService;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class DataController {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    private StockService stockService ;

    @GetMapping("/stocks")
    public List<Stock> getStocks() {
        return stockService.getStocks();
    }

    @GetMapping("/get/bsp/{date}")
    public PagedModel<LoadBspTicker> getBspByDate(
            @PathVariable String date,
            @PageableDefault(size = 1000) Pageable pageable
    ) {
        Page<LoadBspTicker> pages = stockService.getBspsByPriceDate(date, pageable);
        return new PagedModel<>(pages);
    }


    @GetMapping("/get/dsp/{date}")
    public PagedModel<LoadDspTicker> getDspByDate(
            @PathVariable String date,
            @PageableDefault(size = 1000) Pageable pageable
    ) {
        return new PagedModel<>(stockService.getDspsByPriceDate(date, pageable));
    }

    @GetMapping("/bsp")
    public List<LoadBspTicker> getBspTicker(@RequestParam String ticker, @RequestParam Optional<String> fromDate) {
        Date date = null;
        if(fromDate.isPresent())
            date = Date.valueOf(fromDate.get());
        return stockService.getBspTickers(ticker,date);
    }

    @GetMapping("/dsp")
    public List<LoadDspTicker> getDspTicker(@RequestParam String ticker, @RequestParam Optional<String> fromDate) {
        Date date = null;
        if(fromDate.isPresent())
            date = Date.valueOf(fromDate.get());
        return stockService.getDspTickers(ticker,date);
    }

}
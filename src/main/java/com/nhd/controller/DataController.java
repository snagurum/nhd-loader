package com.nhd.controller;

import com.nhd.dto.LoadBspTickerDTO;
import com.nhd.dto.LoadDspTickerDTO;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.LocalDate;
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

    @GetMapping("/bsp/search")
    public PagedModel<LoadBspTickerDTO> getBspByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam String ticker,
        @PageableDefault(size = 1000) Pageable pageable){
        Page<LoadBspTickerDTO> pages = null;
        log.info("Ticker = {}, date = {}", ticker, date);
        if(null == ticker)
            pages = stockService.getBspsByPriceDate(date, pageable);
        else if(1 == ticker.length())
            pages = stockService.getBspsByPriceDateAndTickerStartingWith(date, ticker, pageable);
        else
            pages = stockService.getBspsByPriceDateAndTicker(date, ticker, pageable);

        return new PagedModel<>(pages);
    }


    @GetMapping("/dsp/search")
    public PagedModel<LoadDspTickerDTO> getDspByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam String ticker,
        @PageableDefault(size = 1000) Pageable pageable){
            Page<LoadDspTickerDTO> pages = null;
        if(null == ticker)
            pages = stockService.getDspsByPriceDate(date, pageable);
        else
            pages = stockService.getDspsByPriceDateAndTickerStartingWith(date, ticker, pageable);
        return new PagedModel<>(pages);
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
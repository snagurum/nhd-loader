package com.nhd.controller;

import com.nhd.dto.LoadBspTickerDTO;
import com.nhd.dto.LoadDspTickerDTO;
import com.nhd.models.LoadBspTicker;
import com.nhd.models.LoadDspTicker;
import com.nhd.models.Stock;
import com.nhd.service.BspService;
import com.nhd.service.DspService;
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

    private DspService dspService ;

    private BspService bspService ;

    private StockService stockService ;

    @GetMapping("/stocks")
    public List<Stock> getStocks() {
        return stockService.getStocks();
    }

    @GetMapping("/bsp/search")
    public PagedModel<LoadBspTickerDTO> getBspByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) String ticker, // Makes the parameter optional in the URL
        @PageableDefault(size = 1000) Pageable pageable) {

        Page<LoadBspTickerDTO> pages = null;
        log.info("Ticker = {}, date = {}", ticker, date);
        Optional<String> tickerOpt = Optional.ofNullable(ticker).map(String::trim).filter(t -> !t.isEmpty());

        if (tickerOpt.isEmpty()) {
            pages = bspService.getBspsByPriceDate(date, pageable);
        } else {
            String tickerValue = tickerOpt.get();
            if (tickerValue.length() == 1) {
                pages = bspService.getBspsByPriceDateAndTickerStartingWith(date, tickerValue, pageable);
            } else {
                pages = bspService.getBspsByPriceDateAndTicker(date, tickerValue, pageable);
            }
        }
        return new PagedModel<>(pages);
    }


    @GetMapping("/bsp/tradingDates")
    public List<LocalDate> getBspTradingDates(){
        return bspService.getDistinctPriceDates();

    }


    @GetMapping("/dsp/search")
    public PagedModel<LoadDspTickerDTO> getDspByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false)  String ticker,
        @PageableDefault(size = 1000) Pageable pageable){

        Optional<String> tickerOpt = Optional.ofNullable(ticker).map(String::trim).filter(t -> !t.isEmpty());
        Page<LoadDspTickerDTO> pages = null;
        if(tickerOpt.isEmpty()){
            pages = dspService.getDspsByPriceDate(date, pageable);
        }else{
            String tickerValue = tickerOpt.get();
            pages = dspService.getDspsByPriceDateAndTickerStartingWith(date, tickerValue, pageable);
        }
        return new PagedModel<>(pages);
    }


    @GetMapping("/bsp")
    public List<LoadBspTicker> getBspTicker(@RequestParam String ticker, @RequestParam Optional<String> fromDate) {
        Date date = null;
        if(fromDate.isPresent())
            date = Date.valueOf(fromDate.get());
        return bspService.getBspTickers(ticker,date);
    }

    @GetMapping("/dsp")
    public List<LoadDspTicker> getDspTicker(@RequestParam String ticker, @RequestParam Optional<String> fromDate) {
        Date date = null;
        if(fromDate.isPresent())
            date = Date.valueOf(fromDate.get());
        return dspService.getDspTickers(ticker,date);
    }

}
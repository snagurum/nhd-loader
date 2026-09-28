package com.nhd.service;

import java.sql.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import com.nhd.models.LoadBspTicker;
import com.nhd.models.LoadDspTicker;
import com.nhd.models.LoadTicker;
import com.nhd.models.Stock;
import com.nhd.service.repo.LoadBspTickerRepository;
import com.nhd.service.repo.LoadDspTickerRepository;
import com.nhd.service.repo.LoadTickerRepository;
import com.nhd.service.repo.StockRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StockServiceImpl implements StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    LoadTickerRepository loadTickerRepo;

    LoadDspTickerRepository loadDspRepo;

    StockRepository stockRepo;

    LoadBspTickerRepository loadBspTickersRepo;


    public List<Stock> getActiveTickers(){
        return stockRepo.getActiveTickers();
    }

    public List<Stock> noHistoryStocks(){
        return stockRepo.noHistoryStocks();
    }


    public List<Stock> noHistoryStocksWithLimit(int rowsCount){
        return stockRepo.noHistoryStocksWithLimit(Limit.of(rowsCount));
    }

    public List<Stock> getStocks(){ return Streamable.of(stockRepo.findAll()).toList();}

    public Stock saveStock(Stock stock){
        return stockRepo.save(stock);
    }

    public void saveAllLoadTickers(List<LoadTicker> tickers){

        log.info("truncating load_tickers table");
        loadTickerRepo.truncateTable();
        log.info("populating load_tickers table {}", tickers.size());
        loadTickerRepo.saveAll(tickers);
        log.info("marking new tickers...");
        loadTickerRepo.markNewTickers();
        log.info("adding new tickers");
        loadTickerRepo.addNewStocks();
    }

    public void saveAllLoadDspTickers(List<LoadDspTicker> tickers) {
        loadDspRepo.saveAll(tickers);
    }

    public List<LoadDspTicker> getDspTickers(String ticker, Date date){
        if(date == null){
            return loadDspRepo.findByTicker(ticker);
        } else {
            return loadDspRepo.findByTickerFromDate(ticker, date);
        }
    }

    public List<LoadBspTicker> getBspTickers(String ticker, Date date){
        if(date == null){
            return loadBspTickersRepo.findByTicker(ticker);
        } else {
            return loadBspTickersRepo.findByTickerFromDate(ticker, date);
        }
    }

    public void saveAllLoadBspTickers(List<LoadBspTicker> tickers){
        loadBspTickersRepo.saveAll(tickers);
    }

    public Page<LoadBspTicker> getBspsByPriceDate(String date, Pageable pageable) {
        return loadBspTickersRepo.findByPriceDate(date, pageable);
    }

    public Page<LoadDspTicker> getDspsByPriceDate(String date, Pageable pageable) {
        return loadDspRepo.findByPriceDate(date, pageable);
    }
}

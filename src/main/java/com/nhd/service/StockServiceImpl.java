package com.nhd.service;

import java.sql.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import com.nhd.models.LoadBulkTickers;
import com.nhd.models.LoadDspTickers;
import com.nhd.models.LoadTickers;
import com.nhd.models.Stock;
import com.nhd.service.repo.LoadBulkTickersRepository;
import com.nhd.service.repo.LoadDspTickersRepository;
import com.nhd.service.repo.LoadTickersRepository;
import com.nhd.service.repo.StockRepository;

@Service
public class StockServiceImpl implements StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    @Autowired
    LoadTickersRepository loadTickerRepo;

    @Autowired
    LoadDspTickersRepository loadDspRepo;

    @Autowired
    StockRepository stockRepo;

    @Autowired
    LoadBulkTickersRepository loadBulkTickersRepo;


    public List<Stock> getActiveTickers(){
        return stockRepo.getActiveTickers();
    }

    public List<Stock> noHistoryStocks(){
        return stockRepo.noHistoryStocks();
    }


    public List<Stock> noHistoryStocksWithLimit(int rowsCount){
        return stockRepo.noHistoryStocksWithLimit(rowsCount);
    }

    public List<Stock> getStocks(){ return Streamable.of(stockRepo.findAll()).toList();}

    public Stock saveStock(Stock stock){
        return stockRepo.save(stock);
    }

    public void saveAllLoadTickers(List<LoadTickers> tickers){

        log.info("truncating load_tickers table");
        loadTickerRepo.truncateTable();
        log.info("populating load_tickers table {}", tickers.size());
        loadTickerRepo.saveAll(tickers);
        log.info("marking new tickers...");
        loadTickerRepo.markNewTickers();
        log.info("adding new tickers");
        loadTickerRepo.addNewStocks();
    }

    public void saveAllLoadDspTickers(List<LoadDspTickers> tickers) {
        loadDspRepo.saveAll(tickers);
    }

    public List<LoadDspTickers> getDspTickers(String ticker, Date date){
        if(date == null){
            return loadDspRepo.findBYTicker(ticker);
        } else {
            return loadDspRepo.findBYTickerFromDate(ticker, date);
        }
    }

    public List<LoadBulkTickers> getBspTickers(String ticker, Date date){
        if(date == null){
            return loadBulkTickersRepo.findBYTicker(ticker);
        } else {
            return loadBulkTickersRepo.findBYTickerFromDate(ticker, date);
        }
    }

    public void saveAllLoadBulkTickers(List<LoadBulkTickers> tickers){
        loadBulkTickersRepo.saveAll(tickers);
    }
}

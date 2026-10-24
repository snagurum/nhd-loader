package com.nhd.service.impl;

import com.nhd.models.Holiday;
import com.nhd.models.LoadTicker;
import com.nhd.models.Stock;
import com.nhd.service.StockService;
import com.nhd.service.repo.HolidayRepository;
import com.nhd.service.repo.LoadTickerRepository;
import com.nhd.service.repo.StockRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class StockServiceImpl implements StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    LoadTickerRepository loadTickerRepo;

    StockRepository stockRepo;


    HolidayRepository holidayRepository;


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

    public boolean isTodayAHoliday() {
        return holidayRepository.existsByHolidayDateAndActiveTrue(LocalDate.now());
    }

    public Holiday findByHolidayDate(LocalDate date){
        return holidayRepository.findByHolidayDate(date);
    }

    public void saveAllHolidays(List<Holiday> holidays){
        holidayRepository.saveAll(holidays);
    }
}

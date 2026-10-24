package com.nhd.service;

import com.nhd.models.Holiday;
import com.nhd.models.LoadTicker;
import com.nhd.models.Stock;

import java.time.LocalDate;
import java.util.List;

public interface StockService{

        List<Stock> getActiveTickers();

        List<Stock> noHistoryStocks();

        List<Stock> noHistoryStocksWithLimit(int rowsCount);

        List<Stock> getStocks();

        Stock saveStock(Stock ticker);

        void saveAllLoadTickers(List<LoadTicker> tickers);

        public boolean isTodayAHoliday();

        public Holiday findByHolidayDate(LocalDate holidayDate);

        public void saveAllHolidays(List<Holiday> holidays);
}
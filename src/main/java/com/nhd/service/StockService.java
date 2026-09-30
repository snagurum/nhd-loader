package com.nhd.service;

import java.sql.Date;
import java.util.List;

import com.nhd.models.LoadBspTicker;
import com.nhd.models.LoadDspTicker;
import com.nhd.models.LoadTicker;
import com.nhd.models.Stock;


public interface StockService{

        List<Stock> getActiveTickers();

        List<Stock> noHistoryStocks();

        List<Stock> noHistoryStocksWithLimit(int rowsCount);

        List<Stock> getStocks();

        Stock saveStock(Stock ticker);

        void saveAllLoadTickers(List<LoadTicker> tickers);

        void saveAllLoadDspTickers(List<LoadDspTicker> tickers);

        void saveAllLoadBspTickers(List<LoadBspTicker> tickers);

        List<LoadDspTicker> getDspTickers(String ticker, Date date);

        List<LoadBspTicker> getBspTickers(String ticker, Date date);
}
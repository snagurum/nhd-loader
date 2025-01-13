package com.nhd.service;

import java.sql.Date;
import java.util.List;

import com.nhd.models.LoadBulkTickers;
import com.nhd.models.LoadDspTickers;
import com.nhd.models.LoadTickers;
import com.nhd.models.Stock;


public interface StockService{

        List<Stock> getActiveTickers();

        List<Stock> noHistoryStocks();

        List<Stock> noHistoryStocksWithLimit(int rowsCount);

        List<Stock> getStocks();

        Stock saveStock(Stock ticker);

        void saveAllLoadTickers(List<LoadTickers> tickers);

        void saveAllLoadDspTickers(List<LoadDspTickers> tickers);

        void saveAllLoadBulkTickers(List<LoadBulkTickers> tickers);

        List<LoadDspTickers> getDspTickers(String ticker, Date date);

        List<LoadBulkTickers> getBspTickers(String ticker, Date date);
}
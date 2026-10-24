package com.nhd.service;

import com.nhd.dto.LoadBspTickerDTO;
import com.nhd.models.LoadBspTicker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;


public interface BspService {

    List<StockDateProjection> getBspLoadPendingStockDatesWithLimit(int rowsCount);

    void saveAllLoadBspTickers(List<LoadBspTicker> tickers);

    List<LocalDate> getDistinctPriceDates();

    List<LoadBspTicker> getBspTickers(String ticker, Date date);

    public Page<LoadBspTickerDTO> getBspsByPriceDate(LocalDate date, Pageable pageable);

    public Page<LoadBspTickerDTO> getBspsByPriceDateAndTicker(LocalDate date, String ticker, Pageable pageable);

    public Page<LoadBspTickerDTO> getBspsByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable);

}

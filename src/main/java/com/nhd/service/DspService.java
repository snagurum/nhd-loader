package com.nhd.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nhd.dto.LoadDspTickerDTO;
import com.nhd.models.LoadDspTicker;

public interface DspService {
    
    void saveAllLoadDspTickers(List<LoadDspTicker> tickers);

    List<LoadDspTicker> getDspTickers(String ticker, Date date);

    public Page<LoadDspTickerDTO> getDspsByPriceDate(LocalDate date, Pageable pageable);

    public Page<LoadDspTickerDTO> getDspsByPriceDateAndTicker(LocalDate date, String ticker, Pageable pageable);

    public Page<LoadDspTickerDTO> getDspsByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable);

}

package com.nhd.service.impl;

import com.nhd.dto.LoadBspTickerDTO;
import com.nhd.models.LoadBspTicker;
import com.nhd.service.BspService;
import com.nhd.service.StockDateProjection;
import com.nhd.service.repo.LoadBspTickerRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.sql.Date;

@Service
@AllArgsConstructor
public class BspServiceImpl implements BspService{
 
    LoadBspTickerRepository loadBspTickersRepo;

    public List<StockDateProjection> getBspLoadPendingStockDatesWithLimit( int rowCount){
        return loadBspTickersRepo.getBspLoadPendingStockDatesWithLimit(Limit.of(rowCount));
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

    public List<LocalDate> getDistinctPriceDates(){
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("dd-MMM-yyyy")
            .toFormatter(Locale.ENGLISH);

        return loadBspTickersRepo.findDistinctPriceDates().stream()
            .map(dateStr -> LocalDate.parse(dateStr, formatter)) // Pass the formatter here
            .collect(Collectors.toList());
    }

    public Page<LoadBspTickerDTO> getBspsByPriceDate(LocalDate date, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadBspTickersRepo.findByPriceDate(formattedDate, pageable)
            .map(this::convertToDTO);
    }

    
    public Page<LoadBspTickerDTO> getBspsByPriceDateAndTicker(LocalDate date, String ticker, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadBspTickersRepo.findByPriceDateAndTicker(formattedDate, ticker, pageable)
            .map(this::convertToDTO);
    }

    public Page<LoadBspTickerDTO> getBspsByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadBspTickersRepo.findByPriceDateAndTickerStartingWith(formattedDate, ticker, pageable)
            .map(this::convertToDTO);
    }
    
    private LoadBspTickerDTO convertToDTO(LoadBspTicker bsp){
        LoadBspTickerDTO bspDto = new LoadBspTickerDTO();
        bspDto.setId(bsp.getId());
        bspDto.setTicker(bsp.getTicker());
        bspDto.setPriceDate(bsp.getPriceDate());
        bspDto.setSeries(bsp.getSeries());
        bspDto.setVwap(bsp.getVwap());
        bspDto.setHigh52(bsp.getHigh52());
        bspDto.setLow52(bsp.getLow52());
        bspDto.setTradeClosePrice(bsp.getTradeClosePrice());
        bspDto.setTradeOpenPrice(bsp.getTradeOpenPrice());
        bspDto.setTradePrevClosePrice(bsp.getTradePrevClosePrice());
        bspDto.setTradeHighPrice(bsp.getTradeHighPrice());
        bspDto.setTradeLowPrice(bsp.getTradeLowPrice());
        bspDto.setTradeValue(bsp.getTradeValue());
        bspDto.setNoOfTradeQty(bsp.getNoOfTradeQty());
        bspDto.setNoOfTrades(bsp.getNoOfTrades());
        return bspDto;
    }
}

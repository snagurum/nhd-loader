package com.nhd.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import com.nhd.dto.LoadBspTickerDTO;
import com.nhd.dto.LoadDspTickerDTO;
import com.nhd.models.Holiday;
import com.nhd.models.LoadBspTicker;
import com.nhd.models.LoadDspTicker;
import com.nhd.models.LoadTicker;
import com.nhd.models.Stock;
import com.nhd.service.repo.HolidayRepository;
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

    HolidayRepository holidayRepository;


    public List<Stock> getActiveTickers(){
        return stockRepo.getActiveTickers();
    }

    public List<StockDateProjection> getBspLoadPendingStockDatesWithLimit( int rowCount){
        return stockRepo.getBspLoadPendingStockDatesWithLimit(Limit.of(rowCount));
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

    public Page<LoadBspTickerDTO> getBspsByPriceDate(LocalDate date, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadBspTickersRepo.findByPriceDate(formattedDate, pageable)
            .map(this::convertToDTO);
    }

    public Page<LoadDspTickerDTO> getDspsByPriceDate(LocalDate date, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadDspRepo.findByPriceDate(formattedDate, pageable)
            .map(this::convertToDTO);
    }

    public Page<LoadBspTickerDTO> getBspsByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadBspTickersRepo.findByPriceDateAndTickerStartingWith(formattedDate, ticker, pageable)
            .map(this::convertToDTO);
    }

    public Page<LoadDspTickerDTO> getDspsByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadDspRepo.findByPriceDateAndTickerStartingWith(formattedDate, ticker, pageable)
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

    private LoadDspTickerDTO convertToDTO(LoadDspTicker dsp){
        LoadDspTickerDTO dspDTO = new LoadDspTickerDTO();
        dspDTO.setId(dsp.getId());
        dspDTO.setCompanyDetails(dsp.getCompanyDetails());
        dspDTO.setPriceDate(dsp.getPriceDate());
        dspDTO.setTicker(dsp.getTicker());
        dspDTO.setTradeDetails(dsp.getTradeDetails());
        return dspDTO;
    }

    public boolean isTodayAHoliday() {
        return holidayRepository.existsByHolidayDateAndActiveTrue(LocalDate.now());
    }

    public Holiday findByHolidayDate(LocalDate date){
        return holidayRepository.findByHolidayDate(date);
    }
}

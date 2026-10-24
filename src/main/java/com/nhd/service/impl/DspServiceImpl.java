package com.nhd.service.impl;

import com.nhd.dto.LoadDspTickerDTO;
import com.nhd.models.LoadDspTicker;
import com.nhd.service.DspService;
import com.nhd.service.repo.LoadDspTickerRepository;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class DspServiceImpl implements DspService{

    LoadDspTickerRepository loadDspRepo;
    
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

    public Page<LoadDspTickerDTO> getDspsByPriceDate(LocalDate date, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadDspRepo.findByPriceDate(formattedDate, pageable)
            .map(this::convertToDTO);
    }

    public Page<LoadDspTickerDTO> getDspsByPriceDateAndTicker(LocalDate date, String ticker,  Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadDspRepo.findByPriceDateAndTicker(formattedDate, ticker, pageable)
            .map(this::convertToDTO);
    }

    public Page<LoadDspTickerDTO> getDspsByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        String formattedDate = date.format(formatter);
        return loadDspRepo.findByPriceDateAndTickerStartingWith(formattedDate, ticker, pageable)
            .map(this::convertToDTO);
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

}

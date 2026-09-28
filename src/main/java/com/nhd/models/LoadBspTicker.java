package com.nhd.models;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@Entity
@Table(name = "load_bsp_ticker", schema = "lt")
public class LoadBspTicker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("chSymbol") 
    private String ticker;

    @JsonProperty("mtimestamp") 
    private String priceDate;

    @JsonProperty("chSeries") 
    private String series;

    @JsonProperty("vwap") 
    private String vwap;

    @JsonProperty("ch52WeekHighPrice") 
    private String high52;

    @JsonProperty("ch52WeekLowPrice") 
    private String low52;

    @JsonProperty("chClosingPrice") 
    private String tradeClosePrice;

    @JsonProperty("chLastTradedPrice") 
    private String ltp;

    @JsonProperty("chOpeningPrice") 
    private String tradeOpenPrice;

    @JsonProperty("chPreviousClsPrice") 
    private String tradePrevClosePrice;
    
    @JsonProperty("chTradeHighPrice") 
    private String tradeHighPrice;

    @JsonProperty("chTradeLowPrice") 
    private String tradeLowPrice;

    @JsonProperty("chTotTradedVal") 
    private String tradeValue;
    
    //volume
    @JsonProperty("chTotTradedQty") 
    private String noOfTradeQty;

    @JsonProperty("chTotalTrades") 
    private String noOfTrades;

    
}

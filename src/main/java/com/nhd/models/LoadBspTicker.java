package com.nhd.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Table(name = "load_bsp_ticker", schema = "lt")
public class LoadBspTicker {

    @Id
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

package com.nhd.models;

import java.sql.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Table(name = "load_bulk_tickers", schema = "lt")
public class LoadBulkTickers {


    @Id
    private Long id;

    private String ticker;

    @JsonProperty("Date") 
    private String priceDate;

    @JsonProperty("series") 
    private String series;

    @JsonProperty("OPEN") 
    private String open;

    @JsonProperty("HIGH") 
    private String high;

    @JsonProperty("LOW") 
    private String low;

    @JsonProperty("PREV. CLOSE") 
    private String prev_close;

    @JsonProperty("ltp") 
    private String ltp;

    @JsonProperty("close") 
    private String close;

    @JsonProperty("vwap") 
    private String vwap;

    @JsonProperty("52W H") 
    private String high52;

    @JsonProperty("52W L") 
    private String low52;

    @JsonProperty("VOLUME") 
    private String volume;

    @JsonProperty("VALUE") 
    private String value;

    @JsonProperty("No of trades") 
    private String noOfTrades;



}
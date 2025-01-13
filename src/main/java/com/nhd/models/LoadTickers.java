package com.nhd.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Table(name = "load_tickers", schema = "lt")
public class LoadTickers {

    @Id
    private Long id;

    @JsonProperty("SYMBOL") 
    private String symbol;

    @JsonProperty("NAME OF COMPANY") 
    private String company;

    @JsonProperty("SERIES") 
    private String series;

    @JsonProperty("DATE OF LISTING")
    private String dateOfListing;

    @JsonProperty("PAID UP VALUE") 
    private String paidUpValue;

    @JsonProperty("MARKET LOT")
    private String marketLot;

    @JsonProperty("ISIN NUMBER")
    private String isinNumber;

    @JsonProperty("FACE VALUE")
    private String faceValue;    

}

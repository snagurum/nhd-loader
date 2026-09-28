package com.nhd.models;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "load_tickers", schema = "lt")
public class LoadTicker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

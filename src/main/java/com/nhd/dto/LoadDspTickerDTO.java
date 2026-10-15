package com.nhd.dto;

import java.sql.Date;

import lombok.Data;


@Data
public class LoadDspTickerDTO {

    private Long id;

    private String ticker;

    private Date priceDate;

    private String companyDetails ;

    private String tradeDetails ;

}

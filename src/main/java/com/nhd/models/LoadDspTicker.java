package com.nhd.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Date;

@Data
@Table(name = "load_dsp_tickers", schema = "lt")
public class LoadDspTicker {

    public LoadDspTicker(){
        this.priceDate = new Date(System.currentTimeMillis());
    }

    @Id
    private Long id;

    private String ticker;

    private Date priceDate;

    private String companyDetails ;

    private String tradeDetails ;

}

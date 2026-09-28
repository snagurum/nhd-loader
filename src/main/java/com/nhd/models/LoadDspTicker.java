package com.nhd.models;

import lombok.Data;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.sql.Date;

@Data
@Entity
@Table(name = "load_dsp_tickers", schema = "lt")
public class LoadDspTicker {

    public LoadDspTicker(){
        this.priceDate = new Date(System.currentTimeMillis());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;

    private Date priceDate;

    private String companyDetails ;

    private String tradeDetails ;

}

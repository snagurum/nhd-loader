package com.nhd.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Date;

@Data
@Table(name = "stocks", schema = "lt")
public class Stock {

    @Id
    private Long id;

    private String ticker;

    private String name;

    private String series;

    private Date dateOfListing;

    private boolean historyLoaded;



}


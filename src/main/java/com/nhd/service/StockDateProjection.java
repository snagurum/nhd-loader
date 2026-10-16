package com.nhd.service;

public interface StockDateProjection {
    String getTicker();
    java.time.LocalDate getLatestDate();
}


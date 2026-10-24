package com.nhd.service.repo;

import com.nhd.models.Stock;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("select s from Stock s where s.active = true")
    List<Stock> getActiveTickers();

    @Query("select s from Stock s where s.historyLoaded = false and s.dateOfListing < current_date")
    List<Stock> noHistoryStocks();

    @Query("select s from Stock s where s.historyLoaded = false and s.dateOfListing < current_date")
    List<Stock> noHistoryStocksWithLimit(Limit limit);

}

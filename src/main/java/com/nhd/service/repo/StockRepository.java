package com.nhd.service.repo;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nhd.models.Stock;

@Repository
public interface StockRepository extends CrudRepository<Stock, Long> {

    @Query("select s.* from lt.stocks s where s.active is true")
    List<Stock> getActiveTickers();

    @Query("select s.* from lt.stocks s where s.history_loaded is false and s.date_of_listing < current_date")
    List<Stock> noHistoryStocks();

    @Query("select s.* from lt.stocks s where s.history_loaded is false and s.date_of_listing < current_date limit :rowsCount")
    List<Stock> noHistoryStocksWithLimit(int rowsCount);
}

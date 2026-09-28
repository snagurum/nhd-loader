package com.nhd.service.repo;

import com.nhd.models.LoadBspTicker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface LoadBspTickerRepository extends JpaRepository<LoadBspTicker, Long> {

    @Query("select s from LoadBspTicker s where s.ticker = :ticker")
    List<LoadBspTicker> findByTicker(@Param("ticker") String ticker);

    @Query("select s from LoadBspTicker s where s.ticker = :ticker and s.priceDate > :fromDate")
    List<LoadBspTicker> findByTickerFromDate(@Param("ticker") String ticker, @Param("fromDate") Date fromDate);

    Page<LoadBspTicker> findByPriceDate(String date, Pageable pageable);

}


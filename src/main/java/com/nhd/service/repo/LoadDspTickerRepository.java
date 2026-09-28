package com.nhd.service.repo;

import com.nhd.models.LoadDspTicker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface LoadDspTickerRepository extends JpaRepository<LoadDspTicker, Long>{

    @Query("select s from LoadDspTicker s where s.ticker = :ticker")
    List<LoadDspTicker> findByTicker(@Param("ticker") String ticker);

    @Query("select s from LoadDspTicker s where s.ticker = :ticker and s.priceDate > :fromDate")
    List<LoadDspTicker> findByTickerFromDate(@Param("ticker") String ticker,@Param("fromDate") Date fromDate);

    Page<LoadDspTicker> findByPriceDate(String date, Pageable pageable);

}

package com.nhd.service.repo;

import com.nhd.models.LoadBspTicker;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;


@Repository
public interface LoadBspTickerRepository extends CrudRepository<LoadBspTicker, Long> {

    @Query("select s.* from lt.load_bulk_ticker s where s.ticker = :ticker")
    List<LoadBspTicker> findBYTicker(@Param("ticker") String ticker);

    @Query("select s.* from lt.load_bulk_ticker s where s.ticker = :ticker and s.price_date < :fromDate")
    List<LoadBspTicker> findBYTickerFromDate(@Param("ticker") String ticker,@Param("fromDate") Date fromDate);

}


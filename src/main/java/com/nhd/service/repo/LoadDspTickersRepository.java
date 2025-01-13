package com.nhd.service.repo;

import com.nhd.models.LoadDspTickers;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface LoadDspTickersRepository extends CrudRepository<LoadDspTickers, Long>{

    @Query("select s.* from lt.load_dsp_ticker s where s.ticker = :ticker")
    List<LoadDspTickers> findBYTicker(@Param("ticker") String ticker);

    @Query("select s.* from lt.load_dsp_ticker s where s.ticker = :ticker and s.price_date < :fromDate")
    List<LoadDspTickers> findBYTickerFromDate(@Param("ticker") String ticker,@Param("fromDate") Date fromDate);

}

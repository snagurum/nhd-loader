package com.nhd.service.repo;

import com.nhd.models.LoadBspTicker;
import com.nhd.service.StockDateProjection;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface LoadBspTickerRepository extends JpaRepository<LoadBspTicker, Long> {

    @Query(value = "select s.ticker as ticker, " +
        "coalesce(max(to_date(l.price_date, 'DD-Mon-YYYY')) + interval '1 day', s.date_of_listing) as latestDate " +
        "from stock s " +
        "left join load_bsp_ticker l on s.ticker = l.ticker " +
        "where s.active = true " +
        "group by s.ticker, s.date_of_listing " +
        "having coalesce(max(to_date(l.price_date, 'DD-Mon-YYYY')), s.date_of_listing) " +
        "not in (current_date, current_date - interval '1 day')",
        nativeQuery = true)
    List<StockDateProjection> getBspLoadPendingStockDatesWithLimit(Limit limit);

    @Query("SELECT DISTINCT t.priceDate FROM LoadBspTicker t")
    List<String> findDistinctPriceDates();

    @Query("select s from LoadBspTicker s where s.ticker = :ticker")
    List<LoadBspTicker> findByTicker(@Param("ticker") String ticker);

    @Query("select s from LoadBspTicker s where s.ticker = :ticker and s.priceDate > :fromDate")
    List<LoadBspTicker> findByTickerFromDate(@Param("ticker") String ticker, @Param("fromDate") Date fromDate);

    Page<LoadBspTicker> findByPriceDate(String date, Pageable pageable);

    Page<LoadBspTicker> findByPriceDateAndTicker(String date, String ticker, Pageable pageable);

    Page<LoadBspTicker> findByPriceDateAndTickerStartingWith(String date, String ticker, Pageable pageable);

}


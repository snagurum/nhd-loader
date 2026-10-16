package com.nhd.service.repo;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nhd.models.Stock;
import com.nhd.service.StockDateProjection;


public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("select s from Stock s where s.active = true")
    List<Stock> getActiveTickers();

    @Query("select s from Stock s where s.historyLoaded = false and s.dateOfListing < current_date")
    List<Stock> noHistoryStocks();

    @Query("select s from Stock s where s.historyLoaded = false and s.dateOfListing < current_date")
    List<Stock> noHistoryStocksWithLimit(Limit limit);

    @Query("select s.ticker as ticker, " +
       "coalesce(max(function('to_date', l.priceDate, 'DD-Mon-YYYY')), " +
       "s.dateOfListing) as latestDate " +
       "from Stock s " +
       "left join LoadBspTicker l on s.ticker = l.ticker " +
       "where s.active = true " +
       "group by s.ticker, s.dateOfListing " +
       "having coalesce(max(function('to_date', l.priceDate, 'DD-Mon-YYYY')), " +
       "s.dateOfListing) not in (current_date, current_date - 1 day)")
    List<StockDateProjection> getBspLoadPendingStockDatesWithLimit(Limit limit);
}

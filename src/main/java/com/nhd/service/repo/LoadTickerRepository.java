package com.nhd.service.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.nhd.models.LoadTicker;



    public interface LoadTickerRepository extends JpaRepository<LoadTicker, Long> {

        @Modifying
        @Transactional
        @Query(value = "truncate table lt.load_tickers", nativeQuery = true)
        public void truncateTable();

        @Modifying
        @Transactional
        @Query(value = "update lt.load_tickers set is_new = true where symbol not in (select ticker from lt.stocks)", nativeQuery = true)
        public void markNewTickers();

        @Modifying
        @Transactional
        @Query(value = "insert into lt.stocks(ticker, name, series, date_of_listing, paid_up_value, market_lot, isin_number, face_value, crd_date, upd_date, crd_by, upd_by) " +
                    "select symbol, company, series, date_of_listing::date, paid_up_value, market_lot, isin_number, face_value, localtimestamp, localtimestamp, 'admin', 'admin' " +
                    "from lt.load_tickers where is_new is true", nativeQuery = true)
        public void addNewStocks();

    }
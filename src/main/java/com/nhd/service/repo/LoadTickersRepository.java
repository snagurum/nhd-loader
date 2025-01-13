package com.nhd.service.repo;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nhd.models.LoadTickers;

@Repository
    public interface LoadTickersRepository extends CrudRepository<LoadTickers, Long> {

        @Modifying
        @Query("truncate table  lt.load_tickers")
        public void truncateTable();

        @Modifying
        @Query("update lt.load_tickers set is_new = true where symbol not in (select ticker from lt.stocks)")
        public void markNewTickers();

        @Modifying
        @Query("insert into lt.stocks(ticker, name, series, date_of_listing, paid_up_value, market_lot, isin_number, face_value, crd_date,upd_date,crd_by,upd_by)" +
                        "select symbol, company, series, date_of_listing::date, paid_up_value, market_lot, isin_number, face_value, localtimestamp,localtimestamp,'admin','admin' from lt.load_tickers where is_new is true")
        public void addNewStocks();

    }
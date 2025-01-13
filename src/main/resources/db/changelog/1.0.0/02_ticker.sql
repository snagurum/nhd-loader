
-- deallocate all;




create table lt.LOAD_TICKERS(
    id                integer primary key generated always as identity,
    symbol            varchar ,
    company           varchar,
    series            varchar,
    date_of_listing   varchar,
    paid_up_value     varchar,
    market_lot        varchar,
    isin_number       varchar,
    face_value        varchar,
    is_new            boolean default false
);

create table lt.STOCKS(
    id                integer primary key generated always as identity,
    ticker           varchar(50),        -- SYMBOL
    name             varchar(200),       -- NAME OF COMPANY
    series           varchar(20),        -- SERIES
    date_of_listing  date,               -- DATE OF LISTING
    paid_up_value    varchar(20),        -- PAID UP VALUE
    market_lot       varchar(20),        -- MARKET LOT
    isin_number      varchar(20),        -- ISIN NUMBER
    face_value       varchar(20),        -- FACE VALUE
    active           boolean default true,
    crd_date         date,
    upd_date         date,
    crd_by           varchar(20),
    upd_by           varchar(20)
) ;


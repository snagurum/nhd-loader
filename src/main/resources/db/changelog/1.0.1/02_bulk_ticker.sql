create table lt.load_bulk_tickers(
    id               integer primary key generated always as identity,
    ticker           varchar,
    price_date       varchar,
    series           varchar,
    open             varchar,
    high             varchar,
    low             varchar,
    prev_close             varchar,
    close             varchar,
    vwap             varchar,
    high52             varchar,
    low52             varchar,
    volume             varchar,
    value             varchar,
    no_of_trades             varchar,
    ltp               varchar
);

alter table lt.stocks add column history_loaded boolean default false;
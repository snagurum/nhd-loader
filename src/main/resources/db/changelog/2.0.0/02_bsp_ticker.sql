create table lt.load_bsp_ticker(
    id               integer primary key generated always as identity,
    ticker           varchar,
    price_date       varchar,
    series           varchar,
    trade_open_price             varchar,
    trade_high_price             varchar,
    trade_low_price             varchar,
    trade_prev_close_price             varchar,
    trade_close_price             varchar,
    vwap             varchar,
    high52             varchar,
    low52             varchar,
    trade_value             varchar,
    no_of_trades             varchar,
    no_of_trade_qty             varchar,
    ltp               varchar
);


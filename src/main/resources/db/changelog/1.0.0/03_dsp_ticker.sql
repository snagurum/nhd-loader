create table lt.load_dsp_tickers(
    id               integer primary key generated always as identity,
    ticker           varchar(50),
    price_date       date default current_date,
    company_details  text,
    trade_details    text
);

CREATE UNIQUE INDEX idx_load_bsp_ticker_unique 
ON lt.load_bsp_ticker (ticker, price_date);

CREATE INDEX idx_load_bsp_ticker_price_date ON lt.load_bsp_ticker (price_date);

ALTER TABLE lt.load_bsp_ticker 
ADD CONSTRAINT uq_load_bsp_ticker_ticker_date UNIQUE (ticker, price_date);

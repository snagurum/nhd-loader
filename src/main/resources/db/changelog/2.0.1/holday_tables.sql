CREATE TABLE lt.holiday (
    holiday_date DATE NOT NULL,
    week_day VARCHAR(9) NOT NULL,
    description VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (holiday_date)
);
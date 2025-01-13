package com.nhd.util;

public enum JobName {
    TICKER,
    DSP_TICKER,
    BSP_TICKER,
    BSP_TICKER_UNIT;

    public String getJobType(){
        return "Loader";
    }
}

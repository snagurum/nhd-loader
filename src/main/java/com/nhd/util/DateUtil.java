package com.nhd.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    static DateTimeFormatter OUTPUT_FORMATTER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String getDateStr_ddMMyyyy(LocalDate date){
        return date.format(OUTPUT_FORMATTER_DD_MM_YYYY);
    }

    
}

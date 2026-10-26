package com.nhd.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DateSplitter {

    public record DatePair(LocalDate startDate, LocalDate endDate) {
        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
            return "(" + startDate.format(formatter) + ", " + endDate.format(formatter) + ")";
        }
    }

    public static List<DatePair> splitByYear(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date {"+ start+ " }cannot be after the end date{" + end +"}.");
        }

        List<DatePair> pairs = new ArrayList<>();
        LocalDate currentStart = start;

        while (currentStart.getYear() < end.getYear()) {
            LocalDate currentEnd = LocalDate.of(currentStart.getYear(), Month.DECEMBER, 31);
            pairs.add(new DatePair(currentStart, currentEnd));
            
            currentStart = LocalDate.of(currentStart.getYear() + 1, Month.JANUARY, 1);
        }

        pairs.add(new DatePair(currentStart, end));
        return pairs;
    }
}
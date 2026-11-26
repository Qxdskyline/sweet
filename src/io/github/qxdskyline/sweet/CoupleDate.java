package io.github.qxdskyline.sweet;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;

final class CoupleDate {
    private CoupleDate() {}

    static long daysBetween(LocalDate start, LocalDate today) {
        return ChronoUnit.DAYS.between(start, today);
    }

    static LocalDate nextAnniversary(String date, boolean yearly, LocalDate today) {
        if (yearly && date.matches("[0-9]{2}-[0-9]{2}")) {
            MonthDay monthDay = MonthDay.parse("--" + date);
            int year = today.getYear();
            return monthDay.atYear(year).isBefore(today)
                ? monthDay.atYear(year + 1) : monthDay.atYear(year);
        }
        LocalDate first = LocalDate.parse(date);
        if (!yearly) return first.isBefore(today) ? null : first;
        int year = Math.max(today.getYear(), first.getYear());
        LocalDate occurrence = first.withYear(year);
        return occurrence.isBefore(today) ? first.withYear(year + 1) : occurrence;
    }
}

package com.tys.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Frontend genelde tarihi UTC ISO ({@code ...Z}) gönderir.
 * {@link LocalDateTime} offset'i düşürüp sakladığı için gün kayması oluşur.
 * Bu yardımcı sınıf tarihleri Europe/Istanbul takvim gününe normalize eder.
 */
public final class ReservationDateUtils {

    public static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

    private ReservationDateUtils() {
    }

    public static LocalDate toLocalDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        if (value.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            return value.toLocalDate();
        }
        return value.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ISTANBUL)
                .toLocalDate();
    }

    public static LocalDateTime toStartOfDay(LocalDateTime value) {
        LocalDate date = toLocalDate(value);
        return date == null ? null : date.atStartOfDay();
    }

    public static LocalDateTime toStartOfDay(LocalDate value) {
        return value == null ? null : value.atStartOfDay();
    }
}

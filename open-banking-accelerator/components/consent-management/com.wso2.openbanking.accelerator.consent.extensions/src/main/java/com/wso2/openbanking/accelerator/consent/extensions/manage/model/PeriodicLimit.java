package com.wso2.openbanking.accelerator.consent.extensions.manage.model;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * This class represents a periodic limit for a consent.
 * It includes the period type, amount, period alignment, cyclic expiry time, and cyclic paid amount.
 */
public class PeriodicLimit {
    private String periodType;
    private double amount;
    private String periodAlignment;
    private long cyclicExpiryTime;
    private double cyclicPaidAmount;


    /**
     * Constructs a new PeriodicLimit with the specified period type, amount, and period alignment.
     * It also calculates and sets the cyclic expiry time and cyclic paid amount.
     *
     * @param periodType the period type
     * @param amount the amount
     * @param periodAlignment the period alignment
     */
    public PeriodicLimit(String periodType, double amount, String periodAlignment) {
        this.periodType = periodType;
        this.amount = amount;
        this.periodAlignment = periodAlignment;
        setCyclicExpiryTime();
        calculateCyclicPaidAmount();
    }


    /**
     * Calculates and sets the cyclic expiry time based on the period type and period alignment.
     */
    private void setCyclicExpiryTime() {
        Instant now = Instant.now();
        Instant expiryTime;

        if (periodAlignment.equalsIgnoreCase("consent")) {
            expiryTime = calculateExpiryTimeForConsent(now);
        } else if (periodAlignment.equalsIgnoreCase("calendar")) {
            expiryTime = calculateExpiryTimeForCalendar(now);
        } else {
            throw new IllegalArgumentException("Invalid PeriodAlignment");
        }

        cyclicExpiryTime = expiryTime.getEpochSecond();
    }


    /**
     * Calculates the expiry time for a consent based on the period type.
     *
     * @param now the current time
     * @return the expiry time for a consent
     */
    private Instant calculateExpiryTimeForConsent(Instant now) {
        switch (periodType.toUpperCase()) {
            case "DAY":
                return now.plus(Duration.ofDays(1));
            case "WEEK":
                return now.plus(Duration.ofDays(7));
            case "FORTNIGHT":
                return now.plus(Duration.ofDays(14));
            case "MONTH":
                return now.plus(Period.ofMonths(1));
            case "HALF-YEAR":
                return now.plus(Period.ofMonths(6));
            case "YEAR":
                return now.plus(Period.ofYears(1));
            default:
                throw new IllegalArgumentException("Invalid PeriodType");
        }
    }

    /**
     * Calculates the expiry time for a calendar based on the period type.
     *
     * @param now the current time
     * @return the expiry time for a calendar
     */
    private Instant calculateExpiryTimeForCalendar(Instant now) {
        LocalDate localDate = now.atZone(ZoneId.systemDefault()).toLocalDate();
        switch (periodType.toUpperCase()) {
            case "DAY":
                return localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            case "WEEK":
                return localDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            case "FORTNIGHT":
                return now.plus(Duration.ofDays(14));
            case "MONTH":
                return localDate.with(TemporalAdjusters.firstDayOfNextMonth()).minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            case "HALF_YEAR":
                return calculateHalfYearExpiry(localDate);
            case "YEAR":
                return localDate.with(TemporalAdjusters.firstDayOfNextYear()).minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            default:
                throw new IllegalArgumentException("Invalid PeriodType");
        }
    }

    /**
     * Calculates the expiry time for a half year.
     *
     * @param localDate the current date
     * @return the expiry time for a half year
     */
    private Instant calculateHalfYearExpiry(LocalDate localDate) {
        Month currentMonth = localDate.getMonth();
        if (currentMonth.getValue() < 7) {
            return localDate.withMonth(6).with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            return localDate.withMonth(12).with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
    }

    /**
     * Calculates and sets the cyclic paid amount based on the period alignment.
     */
    private void calculateCyclicPaidAmount() {
        if (periodAlignment.equalsIgnoreCase("consent")) {
            cyclicPaidAmount = 0;
        } else if (periodAlignment.equalsIgnoreCase("calendar")) {
            LocalDate now = LocalDate.now();
            LocalDate expiryDate = Instant.ofEpochSecond(cyclicExpiryTime).atZone(ZoneId.systemDefault()).toLocalDate();
            long daysUntilExpiry = ChronoUnit.DAYS.between(now, expiryDate);
            double applicableAmount = (amount / getDivisorBasedOnPeriodType()) * daysUntilExpiry;
            cyclicPaidAmount = amount - applicableAmount;
        }
    }

    /**
     * Returns the divisor based on the period type.
     *
     * @return the divisor based on the period type
     */
    private int getDivisorBasedOnPeriodType() {
        switch (periodType.toUpperCase()) {
            case "DAY":
                return 1;
            case "WEEK":
                return 7;
            case "FORTNIGHT":
                return 14;
            case "MONTH":
                return LocalDate.now().lengthOfMonth();
            case "HALF-YEAR":
                return LocalDate.now().getMonth().length(LocalDate.now().isLeapYear()) * 6;
            case "YEAR":
                return LocalDate.now().isLeapYear() ? 366 : 365;
            default:
                throw new IllegalArgumentException("Invalid PeriodType");
        }
    }
}

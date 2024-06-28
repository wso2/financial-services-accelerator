package com.wso2.openbanking.accelerator.consent.extensions.manage.model;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.util.PeriodicTypesEnum;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * This class represents a periodic limit for a VRP consent.
 * It includes the period type, amount, period alignment, cyclic expiry time, and cyclic paid amount.
 */
public class PeriodicLimit {
    private final String periodType;
    private BigDecimal amount;
    private String periodAlignment;
    private long cyclicExpiryTime;
    private BigDecimal cyclicRemainingAmount;

    /**
     * Constructs a new PeriodicLimit with the specified period type, amount, and period alignment.
     * It also calculates and sets the cyclic expiry time and cyclic paid amount.
     *
     * @param periodType the period type
     * @param amount the amount
     * @param periodAlignment the period alignment
     */
    public PeriodicLimit(String periodType, BigDecimal amount, String periodAlignment) {
        this.periodType = periodType;
        this.amount = amount;
        this.periodAlignment = periodAlignment;
        setCyclicExpiryTime();
        calculateCyclicPaidAmount();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPeriodAlignment() {
        return periodAlignment;
    }

    public void setPeriodAlignment(String periodAlignment) {
        this.periodAlignment = periodAlignment;
    }

    public long getCyclicExpiryTime() {
        return cyclicExpiryTime;
    }

    public BigDecimal getCyclicRemainingAmount() {
        return cyclicRemainingAmount;
    }

    public void setCyclicRemainingAmount(BigDecimal cyclicRemainingAmount) {
        this.cyclicRemainingAmount = cyclicRemainingAmount;
    }

    /**
     * Calculates and sets the cyclic expiry time based on the period type and period alignment.
     */
    public void setCyclicExpiryTime() {
        Instant now = Instant.now();
        Instant expiryTime;

        if (periodAlignment.equalsIgnoreCase(ConsentExtensionConstants.CONSENT)) {
            expiryTime = calculateExpiryTimeForConsent(now);
        } else if (periodAlignment.equalsIgnoreCase(ConsentExtensionConstants.CALENDAR)) {
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
        PeriodicTypesEnum periodType = PeriodicTypesEnum.valueOf(this.periodType.toUpperCase());
        switch (periodType) {
            case DAY:
                return now.plus(Duration.ofDays(1));
            case WEEK:
                return now.plus(Duration.ofDays(7));
            case FORTNIGHT:
                return now.plus(Duration.ofDays(14));
            case MONTH:
                return now.plus(Period.ofMonths(1));
            case HALF_YEAR:
                return now.plus(Period.ofMonths(6));
            case YEAR:
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
        PeriodicTypesEnum periodType = PeriodicTypesEnum.valueOf(this.periodType.toUpperCase());
        switch (periodType) {
            case DAY:
                return localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            case WEEK:
                return localDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).plusDays(1).
                        atStartOfDay(ZoneId.systemDefault()).toInstant();
            case FORTNIGHT:
                return now.plus(Duration.ofDays(14));
            case MONTH:
                return localDate.with(TemporalAdjusters.firstDayOfNextMonth()).
                        atStartOfDay(ZoneId.systemDefault()).toInstant();
            case HALF_YEAR:
                return calculateHalfYearExpiry(localDate);
            case YEAR:
                return localDate.with(TemporalAdjusters.firstDayOfNextYear()).atStartOfDay(ZoneId.systemDefault())
                        .toInstant();
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
            return localDate.withMonth(6).with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
        } else {
            return localDate.withMonth(12).with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
        }
    }

    /**
     * Calculates and sets the cyclic paid amount based on the period alignment.
     */
    private void calculateCyclicPaidAmount() {
        if (periodAlignment.equalsIgnoreCase(ConsentExtensionConstants.CONSENT)) {
            cyclicRemainingAmount = BigDecimal.valueOf(0);
        } else if (periodAlignment.equalsIgnoreCase(ConsentExtensionConstants.CALENDAR )) {
            LocalDate now = LocalDate.now();
            LocalDate expiryDate = Instant.ofEpochSecond(cyclicExpiryTime).atZone(ZoneId.systemDefault()).toLocalDate();
            BigDecimal divisor = BigDecimal.valueOf(PeriodicTypesEnum.valueOf(this.periodType.toUpperCase()).getDivisor());
            BigDecimal days = BigDecimal.valueOf(ChronoUnit.DAYS.between(now, expiryDate));
            cyclicRemainingAmount = amount.divide(divisor, RoundingMode.HALF_UP).multiply(days).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public boolean validateAmountWithControlParameters(Double amountValue, JSONObject controlParameters) {
        BigDecimal instructedAmount = BigDecimal.valueOf(amountValue);
        BigDecimal maxIndividualAmount = BigDecimal.valueOf(controlParameters.getDouble(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT));

        if (instructedAmount.compareTo(maxIndividualAmount) > 0) {
            return false;
        }

        JSONArray periodicLimits = controlParameters.getJSONArray(ConsentExtensionConstants.PERIODIC_LIMITS);
        long currentMoment = System.currentTimeMillis() / 1000;

        for (int i = 0; i < periodicLimits.length(); i++) {
            JSONObject limit = periodicLimits.getJSONObject(i);
            BigDecimal amount = BigDecimal.valueOf(limit.getDouble(ConsentExtensionConstants.AMOUNT));
            long cyclicExpiryTime = limit.getLong(ConsentExtensionConstants.CYCLIC_EXPIRY_TIME);
            BigDecimal cyclicRemainingAmount = BigDecimal.valueOf(limit.getDouble(ConsentExtensionConstants.CYCLIC_REMAINING_AMOUNT));

            String periodType = limit.getString(ConsentExtensionConstants.PERIOD_TYPE);
            String periodAlignment = limit.getString(ConsentExtensionConstants.PERIOD_ALIGNMENT);

            PeriodicLimit periodicLimit = new PeriodicLimit(periodType, amount, periodAlignment);

            if (currentMoment <= cyclicExpiryTime) {
                if (instructedAmount.compareTo(cyclicRemainingAmount) > 0) {
                    return false;
                }
            } else {
                while(currentMoment > periodicLimit.getCyclicExpiryTime()) {
                    periodicLimit.setCyclicExpiryTime();
                }
                cyclicRemainingAmount = amount;
                if (instructedAmount.compareTo(cyclicRemainingAmount) > 0) {
                    return false;
                } else {
                    cyclicRemainingAmount = cyclicRemainingAmount.subtract(instructedAmount);
                }
            }
        }
        return true;
    }
}

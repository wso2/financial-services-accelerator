/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.openbanking.accelerator.consent.extensions.manage.model;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.util.PeriodicTypesEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
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

        if (periodAlignment.equals(ConsentExtensionConstants.CONSENT)) {
            expiryTime = calculateExpiryTimeForConsent(now);
        } else if (periodAlignment.equals(ConsentExtensionConstants.CALENDAR)) {
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
                return localDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay(ZoneId.systemDefault())
                        .toInstant();
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
            return localDate.withMonth(12).with(TemporalAdjusters.lastDayOfMonth())
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
        }
    }

    /**
     * Calculates and sets the cyclic paid amount based on the period alignment.
     */
    private void calculateCyclicPaidAmount() {
        
        if (periodAlignment.equalsIgnoreCase(ConsentExtensionConstants.CONSENT)) {
            cyclicRemainingAmount = BigDecimal.valueOf(0);
        } else if (periodAlignment.equalsIgnoreCase(ConsentExtensionConstants.CALENDAR)) {
            LocalDate now = LocalDate.now();
            LocalDate expiryDate = Instant.ofEpochSecond(cyclicExpiryTime).atZone(ZoneId.systemDefault())
                    .toLocalDate();
            BigDecimal divisor = BigDecimal.valueOf(PeriodicTypesEnum.valueOf(this.periodType.toUpperCase())
                    .getDivisor());
            BigDecimal days = BigDecimal.valueOf(ChronoUnit.DAYS.between(now, expiryDate));
            cyclicRemainingAmount = amount.divide(divisor, RoundingMode.HALF_UP).multiply(days)
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }
}

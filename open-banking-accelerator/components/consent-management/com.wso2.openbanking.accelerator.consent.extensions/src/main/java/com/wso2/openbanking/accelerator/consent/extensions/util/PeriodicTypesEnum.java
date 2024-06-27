package com.wso2.openbanking.accelerator.consent.extensions.util;

import java.time.LocalDate;

public enum PeriodicTypesEnum {

    DAY("Day"),

    WEEK("Week"),

    FORTNIGHT("Fortnight"),

    MONTH("Month"),

    HALF_YEAR("Half-Year"),

    YEAR("Year");

    private String value;

    PeriodicTypesEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Returns the divisor based on the period type.
     *
     * @return the divisor based on the period type
     */
    public int getDivisor() {
        switch (this) {
            case DAY:
                return 1;
            case WEEK:
                return 7;
            case FORTNIGHT:
                return 14;
            case MONTH:
                return LocalDate.now().lengthOfMonth();
            case HALF_YEAR:
                return LocalDate.now().isLeapYear() ? 181 : 180;
            case YEAR:
                return LocalDate.now().isLeapYear() ? 366 : 365;
            default:
                throw new IllegalArgumentException("Invalid PeriodType");
        }
    }
}

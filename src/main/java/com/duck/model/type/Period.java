package com.duck.model.type;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a time period defined by a start and end LocalDate.
 * Provides methods to check if a date falls within the period and
 * to calculate the number of days between the boundaries.
 */
public class Period {
    private LocalDate startDate;
    private LocalDate endDate;

    /** Constructs an empty Period. */
    public Period() {}

    /**
     * Constructs a Period with the given start and end dates.
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     */
    public Period(LocalDate startDate, LocalDate endDate) {
        // set range
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Checks whether a given date falls within this period.
     * @param date the date to check
     * @return true if the date is between startDate and endDate inclusive
     */
    public boolean contains(LocalDate date) {
        // validate inclusion
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /** @return the number of days between start and end */
    public long getDaysBetween() {
        // calculate duration
        return ChronoUnit.DAYS.between(this.startDate, this.endDate);
    }

    /** @return the start date */
    public LocalDate getStartDate() {
        return this.startDate;
    }

    /** @return the end date */
    public LocalDate getEndDate() {
        return this.endDate;
    }

    /** @param startDate the new start date */
    public void setStartDate(LocalDate startDate) {
        // update start
        this.startDate = startDate;
    }

    /** @param endDate the new end date */
    public void setEndDate(LocalDate endDate) {
        // update end
        this.endDate = endDate;
    }
}
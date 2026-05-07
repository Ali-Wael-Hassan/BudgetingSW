package com.duck.model.type;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Period {
    private LocalDate startDate;
    private LocalDate endDate;

    public Period() {}

    public Period(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public long getDaysBetween() {
        return ChronoUnit.DAYS.between(this.startDate, this.endDate);
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }
}
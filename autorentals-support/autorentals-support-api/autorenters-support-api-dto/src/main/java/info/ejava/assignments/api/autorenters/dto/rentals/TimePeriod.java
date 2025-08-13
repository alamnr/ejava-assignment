package info.ejava.assignments.api.autorenters.dto.rentals;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Objects;

/**
 * This class provides a convenience mechanism for tracking a start
 * and end date, determining their relationship to other time periods,
 * and anything else that can offload start/end data evaluations from
 * the service logic. Note: dates are inclusive. A startDate represents
 * the first day and the endDate represents the lastDay. That means they
 * can be the same date and represent 1 day.
 */
@Data
@Builder
@With
public class TimePeriod implements Comparable<TimePeriod> {
    private final LocalDate startDate; //inclusive
    private final LocalDate endDate;   //inclusive

    public TimePeriod(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
        Assert.notNull(startDate,"startDate is required");
        endDate = Objects.requireNonNullElse(endDate, startDate);
        Assert.isTrue(!startDate.isAfter(endDate),"startDate cannot be after endDate");
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public TimePeriod(@NotNull LocalDate startDate) {
        this(startDate, startDate);
    }
    public TimePeriod(@NotNull LocalDate startDate, @Positive int days) {
        Assert.notNull(startDate,"startDate is required");
        Assert.isTrue(days>0, "positive days required");
        this.startDate = startDate;
        this.endDate = startDate.plusDays(days-1); //endDate is inclusive
    }

    @Positive
    public int getDays() {
        return getPeriod().getDays();
    }
    @NotNull
    public Period getPeriod() {
        //Period.between endDate is exclusive
        return Period.between(startDate, endDate.plusDays(1));
    }

    /**
     * Evaluate whether the date provided is between the startDate and endDate,
     * both inclusive.
     * @param date to test between
     * @return true if between the start and end dates, inclusive
     */
    public boolean isBetween(LocalDate date) {
        if (null==date) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Evaluate whether the time period provided has a start or end date that
     * overlaps with this one.
     * @param other timePeriod to evaluate
     * @return true if timePeriods overlap
     */
    public boolean isOverlap(TimePeriod other) {
        if (null==other || other.getStartDate()==null || other.getEndDate()==null) {
            return false;
        }
        return isBetween(other.getStartDate()) || isBetween(other.getEndDate());
    }

    /**
     * Return a timeperiod like this one, just N days prior or after.
     * @param days amount to shift the new TimePeriod
     * @return timeperiod
     */
    public TimePeriod slide(int days) {
        return slide(Period.ofDays(days));
    }

    /**
     * Return a timeperiod like this one an amount before or after this one.
     * @param amount
     * @return timeperiod
     */
    public TimePeriod slide(TemporalAmount amount) {
        return new TimePeriod(startDate.plus(amount), endDate.plus(amount));
    }
    public TimePeriod prev() {
        return new TimePeriod(startDate.minusDays(getDays()),getDays());
    }

    /**
     * Return a timePeriod that starts the day after endDate
     * @return next time period like this
     */
    public TimePeriod next() {
        return new TimePeriod(endDate.plusDays(1),getDays());
    }

    /**
     * Creates a timeperiod according to what was provided for startDate and endDate
     * @param startDate will be set to endDate if startDate not provided
     * @param endDate will be set to startDate if endDate not provided
     * @return timeperiod
     */
    public static TimePeriod create(LocalDate startDate, LocalDate endDate) {
        if (null==startDate && null==endDate) {
            return null;
        } else if (null==startDate){
            startDate = endDate;
        }
        return new TimePeriod(startDate, endDate);
    }

    /**
     * Returns negative if this instance does not overlap and is before the
     * provided instance. Returns positive if it does not overlap and after the
     * other instance. Otherwise 0 is returned.
     * @param rhs the object to be compared.
     * @return 0 if overlapped, -1 before, and +1 after
     */
    @Override
    public int compareTo(@NotNull TimePeriod rhs) {
        Assert.notNull(rhs,"timePeriod required");

        if (endDate.isBefore(rhs.startDate)) {
            return -1;
        } else if (startDate.isAfter(rhs.endDate)) {
            return 1;
        } else {
            return 0; //overlap
        }
    }
}

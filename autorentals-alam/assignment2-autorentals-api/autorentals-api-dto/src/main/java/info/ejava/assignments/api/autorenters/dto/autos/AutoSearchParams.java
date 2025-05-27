package info.ejava.assignments.api.autorenters.dto.autos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class AutoSearchParams {
    private Integer minPassengersInclusive;
    private Integer maxPassengersInclusive;
    // daily rate expressed in simple Integer values
    private Integer minDailyRateInclusive;
    private Integer maxDailyRateExclusive;
    private Integer pageNumber;
    private Integer pageSize;

    public AutoSearchParams passengersWithin(int minInclusive, int maxInclusive) {
        this.minPassengersInclusive = minInclusive;
        this.maxPassengersInclusive = maxInclusive;
        return this;
    }

    public AutoSearchParams dailyRateWithin(int minInclusive, int maxExclusive) {
        this.minDailyRateInclusive = minInclusive;
        this.maxDailyRateExclusive = maxExclusive;
        return this;
    }

    public AutoSearchParams page(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        return this;
    }

}

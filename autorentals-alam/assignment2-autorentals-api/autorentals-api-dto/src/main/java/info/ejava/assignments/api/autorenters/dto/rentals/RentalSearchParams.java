package info.ejava.assignments.api.autorenters.dto.rentals;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class RentalSearchParams {
    private String autoId;
    private String renterId;
    private TimePeriod timePeriod;
    private Integer pageNumber;
    private Integer pageSize;

    public LocalDate getStartDate() {
        return null==timePeriod ? null : timePeriod.getStartDate();
    }
    public LocalDate getEndDate() {
        return null==timePeriod ? null : timePeriod.getEndDate();
    }
}

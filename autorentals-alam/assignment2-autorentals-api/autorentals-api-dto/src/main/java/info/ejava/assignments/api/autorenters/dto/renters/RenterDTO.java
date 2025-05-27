package info.ejava.assignments.api.autorenters.dto.renters;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class RenterDTO {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String email;

    @JsonIgnore
    private String username;
}

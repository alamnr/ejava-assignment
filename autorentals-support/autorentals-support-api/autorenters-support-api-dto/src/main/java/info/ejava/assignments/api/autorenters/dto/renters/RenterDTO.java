package info.ejava.assignments.api.autorenters.dto.renters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDate;

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

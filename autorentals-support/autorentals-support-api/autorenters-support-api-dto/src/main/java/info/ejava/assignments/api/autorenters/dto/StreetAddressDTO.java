package info.ejava.assignments.api.autorenters.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StreetAddressDTO {
    private String street;
    private String city;
    private String state;
    private String zip;
}

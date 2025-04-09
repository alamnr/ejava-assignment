package info.ejava.alamnr.assignment1.configprops.rentals.properties;

import lombok.ToString;
import lombok.Value;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Value
@ToString
@Validated
public class AddressProperties {
    
    @NotBlank
    private final String city;
    private final String state;

    @ConstructorBinding
    public AddressProperties(String city, String  state){
        this.city = city;
        this.state = state;
    }

    public AddressProperties(){
        this.city = "default";
        this.state = "default";
    }

    
}

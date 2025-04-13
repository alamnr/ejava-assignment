package info.ejava.alamnr.assignment1.autorentals.logging.repo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@Builder
@AllArgsConstructor
public class AutoRentalDTO {

    private String autoId;
    private String renterId;
    private BigDecimal amount;
    

    @Override
    @SneakyThrows
    public String toString() {
        String result = "AutoRental{" +
                "autoId='" + autoId + '\'' +
                ", renterId='" + renterId + '\'' +
                ", amount=" + amount +
                '}';
        return result;
    }
    
}

package info.ejava.assignments.security.autorenters.svc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
public class Accounts {
    private final List<AccountProperties> accounts = new ArrayList<>();
}

package info.ejava.assignments.security.autorenters.svc;

import java.util.Collections;
import java.util.List;

import lombok.Value;

@Value
public class AccountProperties {
    private final String username;
    private final String password;
    private List<String> authorities;

    public List<String> getAuthorities(){
        return null==authorities? Collections.emptyList() : authorities;
    }
}

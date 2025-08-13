package info.ejava.assignments.security.autorenters.svc.rentals;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.security.autorenters.svc.AccountProperties;
import info.ejava.assignments.security.autorenters.svc.Accounts;
import info.ejava.examples.common.web.ServerConfig;

//@SpringBootTest(classes= { ...
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles({"test", "authorities"})
//@DisplayName("Part B1: Authorities")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class B1_AuthoritiesNTest {
    @Autowired
    RestTemplate anonymousUser;
    @Autowired
    @Qualifier("usernameMap")
    private Map<String,RestTemplate> authnUsers;
    @Autowired
    Accounts accounts;
    private URI authoritiesUrl;
    private @Autowired Environment env;
    @Autowired(required = false)
    List<PasswordEncoder> passwordEncoder;
    @Autowired(required = false)
    List<UserDetailsService> userDetails;

    @BeforeEach
    void init(@Autowired ServerConfig serverConfig){
        BDDAssumptions.given(userDetails).as("no user details found for accounts").isNotEmpty();
        BDDAssumptions.given(passwordEncoder).as("no password encoder found").isNotEmpty();
        BDDAssumptions.given(env.getActiveProfiles()).as("missing required profile").contains("authorities");

        authoritiesUrl = UriComponentsBuilder.fromUri(serverConfig.getBaseUrl())
                            .path("/api/authorities").build().toUri();

    }

    Stream<Arguments> all_authorities() {
        Assumptions.assumeTrue(null!=accounts,"test class should only run for derived class");
        return accounts.getAccounts().stream()
                    .flatMap(account -> account.getAuthorities().stream())
                    .collect(Collectors.toSet()) // dedup
                    .stream()
                    .map(authority-> Arguments.of(authority));
    }

    @ParameterizedTest(name = "{index} anonymous does not have authority {0}")
    @MethodSource("all_authorities")
    void anonymous_has_noauthorities(String authority){
        // given
        URI url = UriComponentsBuilder.fromUri(authoritiesUrl)
                    .queryParam("authority", authority).build().toUri();
        RequestEntity request = RequestEntity.get(url).build();
        // when
        ResponseEntity<String> response = anonymousUser.exchange(request, String.class);
        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(Boolean.valueOf(response.getBody())).isFalse();

    }

    Stream<Arguments> accounts(){
        Assumptions.assumeTrue(null!=accounts,"test case should run only for derived class");
        List<Arguments> accountAuthority = new ArrayList<>();
        // for(AccountProperties account: accounts.getAccounts()){
        //     for(String authority : account.getAuthorities()){
        //         RestTemplate restTemplate = authnUsers.get(account.getUsername());
        //     accountAuthority.add(Arguments.of(restTemplate,account.getUsername(),authority));    
        //     }
        // }
        accounts.getAccounts().forEach(account -> account.getAuthorities().forEach(authority-> {
            RestTemplate restTemplate = authnUsers.get(account.getUsername());
            accountAuthority.add(Arguments.of(restTemplate,account.getUsername(),authority));
        }));

        return accountAuthority.stream();
    }

    @ParameterizedTest(name = "{index} {1} has authority {2}")
    @MethodSource("accounts")
    void user_has_authorities(RestTemplate authnClient, String username, String authority){
        // given
        URI url = UriComponentsBuilder.fromUri(authoritiesUrl) .queryParam("authority", authority).build().toUri();
        RequestEntity request = RequestEntity.get(url).build();
        // when
        ResponseEntity<String> response = authnClient.exchange(request, String.class);
        // then
        BDDAssertions.then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BDDAssertions.then(Boolean.valueOf(response.getBody())).isTrue();
    }
}

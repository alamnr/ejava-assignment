package info.ejava.assignments.security.autorenters.svc.testapp;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import info.ejava.assignments.security.autorenters.svc.Accounts;


@TestConfiguration(proxyBeanMethods = false)
@Profile("authorities")
@EnableMethodSecurity(prePostEnabled = true)
public class AuthoritiesTestConfiguration {

    @Bean
    public SecurityFilterChain authoritiesHttp(HttpSecurity http) throws Exception {

        http.securityMatchers(m -> m.requestMatchers("/api/**"));

        http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.HEAD).permitAll());
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.GET,"/api/autos/**").permitAll());
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.POST,"/api/autos/query").permitAll());
        // path based authorization
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.DELETE,"/api/autos").hasRole("ADMIN"));
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.GET,"/api/renters").hasRole("MGR"));
        //http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.GET,"/api/renters/*").authenticated());
        http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.DELETE,"/api/renters").hasRole("ADMIN"));

        http.authorizeHttpRequests(cfg->cfg.anyRequest().authenticated());

        http.httpBasic(cfg->{});  //http.httpBasic(Customizer.withDefaults());
        http.csrf(cfg->cfg.disable());
        http.sessionManagement(cfg->cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder, Accounts accounts) {
        //https://github.com/spring-projects/spring-security/commit/3229bfa40ff6937c0bd75edad7e4a132533ce266
        //User.UserBuilder builder = User.builder().passwordEncoder(encoder::encode);

        List<UserDetails> users = accounts.getAccounts().stream()
                                    .map(a -> User.builder().passwordEncoder(password -> encoder.encode(password))//.passwordEncoder(encoder::encode)
                                                .username(a.getUsername())    
                                                .password(a.getPassword())
                                                .authorities(a.getAuthorities().toArray(new String[0]))
                                                .build())
                                    .map(a->a)
                                    .toList();
        return new InMemoryUserDetailsManager(users);

    }
    
}

package info.ejava.assignments.security.autorenters.svc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import info.ejava.assignments.api.autorentals.svc.main.ProvidedApiAutoRenterTestConfiguration;
import info.ejava.examples.common.web.RestTemplateConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Import(ProvidedApiAutoRenterTestConfiguration.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ProvidedAuthorizationTestHelperConfiguration {
    
    private RestTemplate createUser(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory, AccountProperties account) {

        ClientHttpRequestInterceptor[] filters = (null == account) ? new ClientHttpRequestInterceptor[]{} : 
                                                    new ClientHttpRequestInterceptor[]{
                                                        new BasicAuthenticationInterceptor(account.getUsername(), account.getPassword())
                                                    };
        return new RestTemplateConfig().restTemplateDebug(builder,requestFactory, filters);

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(name = "anonymousUser")
    class AnonymousConfiguration {
        @Bean
        public RestTemplate anonymousUser(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory){
            return createUser(builder, requestFactory, null);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile({"userdetails","authorities"})
    class AuthenticatedConfiguration {
        @Bean
        @ConfigurationProperties("autorenters")
        @ConditionalOnMissingBean()
        public Accounts rentalAccounts() {
            return new Accounts();
        }

        private AccountProperties getUserAccount(Accounts accounts, int index){
            if(!accounts.getAccounts().isEmpty()){
                List<AccountProperties> userAccounts = accounts.getAccounts().stream()
                                                        .filter(a -> !a.getAuthorities().contains("ROLE_ADMIN"))
                                                        .filter(a -> !a.getAuthorities().contains("ROLE_MGR"))
                                                        .filter(a -> !a.getAuthorities().contains("PROXY"))
                                                        .toList();
                if(userAccounts.size() <= index ){
                    throw new IllegalStateException("Cannot find user without elevated roles");
                }
                log.info("using account({}) for anAuthUser", userAccounts.get(index));
                return userAccounts.get(index);
            } else {
                throw new IllegalStateException(" no user.name/password or accounts specified");
            }
        }

        private AccountProperties findUserWithAuthority(Accounts accounts, String authority){
            if(!accounts.getAccounts().isEmpty()) {
                AccountProperties account = accounts.getAccounts().stream()
                                            .filter(a -> a.getAuthorities().contains(authority))
                                            .findFirst()
                                            .orElseThrow(() -> new IllegalStateException("cannot find user authority " + authority));
                log.info("using account({}) for {} authority", account,authority);
                return account;
            }   else {
                throw new IllegalStateException("no user.name/password or accounts specified");
            }
        }

        @Bean
        public AccountProperties anAccount(Accounts accounts){
            return getUserAccount(accounts, 0);
        }

        @Bean
        public AccountProperties altAccount(Accounts accounts){
            return getUserAccount(accounts, 1);
        }

        @Bean
        @Profile("authorities")
        public AccountProperties mgrAccount(Accounts accounts){
            return findUserWithAuthority(accounts, "ROLE_MGR");
        }

        @Bean
        @Profile("authorities")
        public AccountProperties proxyAccount(Accounts accounts){
            return findUserWithAuthority(accounts, "PROXY");
        }

        @Bean
        @Profile("authorities")
        public AccountProperties adminAccount(Accounts accounts) {
            return findUserWithAuthority(accounts, "ROLE_ADMIN");
        }

        @Bean
        @Qualifier("usernameMap")
        public Map<String,RestTemplate> authnUsers(RestTemplateBuilder builder, ClientHttpRequestFactory requestFactory,Accounts accounts){
            Map<String,RestTemplate> authnUsers = new HashMap<>();
            for (AccountProperties account : accounts.getAccounts()) {
                RestTemplate restTemplate = createUser(builder, requestFactory, account);
                authnUsers.put(account.getUsername(), restTemplate);                
            }
            return authnUsers;
        }

        @Bean
        @Qualifier("authn")
        public RestTemplate authnUser(@Qualifier("usernameMap")Map<String,RestTemplate> authnUsers, AccountProperties anAccount ) {
            return authnUsers.get(anAccount.getUsername());
        }

        @Bean
        @Qualifier("authn")
        public RestTemplate altUser(@Qualifier("usernameMap") Map<String,RestTemplate> authnUsers, AccountProperties altAccount){
            return authnUsers.get(altAccount.getUsername());
        }

        @Bean
        @Qualifier("authn")
        @Profile("authorities")
        public RestTemplate adminUser(@Qualifier("usernameMap") Map<String,RestTemplate> authnUsers, AccountProperties adminAccount){
            return authnUsers.get(adminAccount.getUsername());
        }

        @Bean
        @Qualifier("authn")
        @Profile("authorities")
        public RestTemplate mgrUser(@Qualifier("usernameMap")Map<String,RestTemplate> authnUsers, AccountProperties mgrAccount){
            return authnUsers.get(mgrAccount.getUsername());
        }

        @Bean
        @Qualifier("authn")
        @Profile("authorities")
        public RestTemplate proxyUser(@Qualifier("usernameMap") Map<String, RestTemplate> authnUsers, AccountProperties proxyAccount){
            return authnUsers.get(proxyAccount.getUsername());
        }

    }
}

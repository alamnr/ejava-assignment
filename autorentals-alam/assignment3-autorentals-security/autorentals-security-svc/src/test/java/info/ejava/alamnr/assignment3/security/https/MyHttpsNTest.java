package info.ejava.alamnr.assignment3.security.https;

import java.net.URI;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.alamnr.assignment3.security.AutoRentalsSecurityApp;
import info.ejava.alamnr.assignment3.security.autorentals.impl.SecurityTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;

@SpringBootTest(classes={AutoRentalsSecurityApp.class,
                        SecurityTestConfiguration.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({
        // turn on final SecurityFilterChain settings
        "authorization",
        // populate with users , credentials and authorities
        "authorities",
        // activate serverside HTTPS properties
        "https",
        //activate any standard test settings, like logger
        "test",
        //get client-side HTTPS properties, including trustStore
        "its"

})
@Slf4j
//@Disabled("Complete and enable")

public class MyHttpsNTest {
  
        @Autowired
        String username;
        @Autowired @Qualifier("altUser")
        private RestTemplate itAuthnUser;
        @Autowired
        private URI whoAmIUrl;
        @Autowired
        private SSLFactory sslFactory;
        @Autowired @Qualifier("httpsRequestFactory")
        ClientHttpRequestFactory requestFactory;

        
        //@Test
        void context() {
                BDDAssertions.then(requestFactory).isNotNull();
                log.info("interceptor size - {}",itAuthnUser.getInterceptors().size());
                
                // Print all interceptors
                for (ClientHttpRequestInterceptor interceptor : itAuthnUser.getInterceptors()) {
                System.out.println("Interceptor: " + interceptor);

                if (interceptor instanceof BasicAuthenticationInterceptor basicAuth) {
                        System.out.println("→ Basic Auth Username: " + basicAuth.getClass());
                        // ⚠️ password is not directly exposed for security reasons
                }
                }

        }

        @Test
        void user_can_call_authenticated(){
                //given a request to an endpoint that accepts only authenticated calls
                URI url = UriComponentsBuilder.fromUri(whoAmIUrl).build().toUri();

                //when called with an authenticated identity
                ResponseEntity<String> responseGet =  itAuthnUser.getForEntity(url, String.class);
                ResponseEntity<String> responsePost = itAuthnUser.postForEntity(url,null, String.class);

                //then expected results returned
                        //status success
                        BDDAssertions.then(responseGet.getStatusCode()).isEqualTo(HttpStatus.OK);
                        BDDAssertions.then(responsePost.getStatusCode()).isEqualTo(HttpStatus.OK);
                        //verified username/identity
                        BDDAssertions.then(responseGet.getBody()).isNotNull();
                        BDDAssertions.then(responsePost.getBody()).isNotNull();
                        BDDAssertions.then(responseGet.getBody()).isEqualTo(responsePost.getBody());
                        log.info("************************* responseGet - {}, post - {}", responseGet.getBody(),responsePost.getBody());
                        BDDAssertions.then(responseGet.getBody()).isEqualTo("sueann");

        }

}
 
package info.ejava.alamnr.assignment3.security.https;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
import info.ejava.assignments.api.autorenters.svc.autos.AutosController;
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

        @Autowired
        AutosController controller;

        /*
         * Dynamic Proxy test 
         * 
         */
        interface Service {
                void perform();
        }

        class RealService implements Service {

                @Override
                public void perform() {
                        log.info("********************* performing real service");
                }
        }

        class LoggingHandler implements InvocationHandler {

                private final Object target;
                public LoggingHandler(Object target){
                        this.target = target;
                }
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        log.info("************ Log before - {}", method.getName());
                        Object result = method.invoke(target, args);
                        log.info("************ Log after - {}", method.getName());
                        return result;
                }
        }
        
        @Test
        void context() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
                // BDDAssertions.then(requestFactory).isNotNull();
                // log.info("interceptor size - {}",itAuthnUser.getInterceptors().size());
                
                // // Print all interceptors
                // for (ClientHttpRequestInterceptor interceptor : itAuthnUser.getInterceptors()) {
                // System.out.println("Interceptor: " + interceptor);

                // if (interceptor instanceof BasicAuthenticationInterceptor basicAuth) {
                //         System.out.println("→ Basic Auth Username: " + basicAuth.getClass());
                //         // ⚠️ password is not directly exposed for security reasons
                // }
                // }

                Class<?> clazz = Class.forName("info.ejava.assignments.api.autorenters.svc.autos.AutosController");
                log.info("************************** class name - {}", clazz.getName());

                // methods
                for (Method method: clazz.getDeclaredMethods() ) {
                        log.info("Method- {}", method.getName() );                        
                }

                // fields
                for (Field field : clazz.getDeclaredFields()) {
                        log.info("******* Field: ", field.getName());
                }

                // create an instance dynamically
                // Object obj = clazz.getDeclaredConstructor().newInstance();

                // List constructor
                for (Constructor<?> cons : clazz.getDeclaredConstructors()) {
                        log.info("************** constructor - {}", cons);
                }


                System.out.println("=== Inspecting RentersController with Reflection ===");

                Class<?> clazz_1 = controller.getClass();
                System.out.println("Controller Class: " + clazz_1.getName());

                for (Field field : clazz_1.getDeclaredFields()) {
                        field.setAccessible(true);
                        Object value = field.get(controller); // get injected value
                        System.out.printf("Field: %-20s | Type: %-30s | Value: %s%n",
                                field.getName(),
                                field.getType().getName(),
                                value != null ? value.getClass().getName() : "null");
                }

                // Debugging injection problems (null beans, multiple implementations).

                // Understanding how Spring proxies your beans (you’ll often see $$EnhancerBySpringCGLIB$$ in the class name).

                // Dynamic Proxy

                Service realService = new RealService();

                Service proxy = (Service) Proxy.newProxyInstance(
                        Service.class.getClassLoader(),
                        new Class[]{Service.class},
                        new LoggingHandler(realService)
                );

                proxy.perform(); // will log before and after

                log.info("************** proxy class - {}", proxy.getClass());

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
 
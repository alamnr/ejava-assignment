package info.ejava.alamnr.assignment3.security.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import info.ejava.assignments.security.autorenters.svc.AccountProperties;
import info.ejava.assignments.security.autorenters.svc.Accounts;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    @Configuration(proxyBeanMethods = false)
    @Profile("anonymous-access") // requirement - 6
    public static class PartA1_AnonymousAccess {

        /**
         * https://github.com/jzheaux/cve-2023-34035-mitigations
         * An explicit MvcRequestMatcher.Builder is necessary when mixing SpringMvc with
         * non-SpringMvc Servlets. Enabling the H2 console puts us in that position.
         * Dissabling (spring.h2.console.enabled=false) or being explicit as to which URI
         * apply to SpringMvc avoids the problem.
         * @param introspector
         * @return
         */
        @Bean
        MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
            return new MvcRequestMatcher.Builder(introspector);
        }

        @Bean
        public WebSecurityCustomizer authzStaticResources(MvcRequestMatcher.Builder mvc){
            return web -> web.ignoring().requestMatchers(mvc.pattern("/content/**"));  // Requirement - 3.a
        }

        @Bean
        @Order(0)
        public SecurityFilterChain authzSecurityFilters(HttpSecurity http, MvcRequestMatcher.Builder mvc, RoleHierarchy roleHierarchy) throws Exception{

            http.securityMatchers(cfg -> cfg.requestMatchers(mvc.pattern("/api/**")));
            
            http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.HEAD).permitAll());  // Requirement: 3.b

            http.authorizeHttpRequests(cfg-> 
                    cfg.requestMatchers(HttpMethod.GET,"/api/autos","/api/autos/**","/api/autorentals","/api/autorentals/**").permitAll()
                     
            ); // Requirement 3.c

            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(HttpMethod.POST,"/api/autos/query").permitAll());  // Req- 3.d
            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(HttpMethod.POST,"/api/autorentals/query").permitAll());

            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(HttpMethod.GET,"/api/renters","/api/renters/**").authenticated());  // Req - 4.a
           
            // Requirement 4.b
            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(HttpMethod.POST,
                            "/api/autos","/api/autos/**","/api/renters","/api/renters/**","/api/autorentals","/api/autorentals/**").authenticated());
            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(HttpMethod.PUT,
                            "/api/autos","/api/autos/**","/api/renters","/api/renters/**","/api/autorentals","/api/autorentals/**").authenticated());
            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(HttpMethod.DELETE,
                            "/api/autos","/api/autos/**","/api/renters","/api/renters/**","/api/autorentals","/api/autorentals/**").authenticated());
            
            
            
            http.csrf(cfg-> cfg.disable());  // Requirement: 1
            return http.build();
        }

        
        //needed mid-way thru lecture
        /*@Bean
        @Profile("roleInheritance")
        static RoleHierarchy roleHierarchy() {
                return RoleHierarchyImpl.withDefaultRolePrefix()
                        .role("ADMIN").implies("CLERK")
                        .role("CLERK").implies("CUSTOMER")
                        .build();
            //legacy
            //        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
            //        roleHierarchy.setHierarchy(StringUtils.join(List.of(
            //                "ROLE_ADMIN > ROLE_CLERK",
            //                "ROLE_CLERK > ROLE_CUSTOMER"
            //        ),System.lineSeparator()));
            //        return roleHierarchy;
        } */


        /**
         * Creates a default RoleHierachy when the examples want straight roles.
         */
        @Bean
        @Profile("!roleInheritance")
        static RoleHierarchy nullHierarchy() {
            return new NullRoleHierarchy();
        }

        /**
         * Creates a custom MethodExpressionHandler that will be picked up by
         * Expression-based security to support RoleInheritance.
         * This is required until the
         * <a href="github.com/spring-projects/spring-security/issues/12783">the following</a>
         * is resolved.
         */
        @Bean
        static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy, ApplicationContext context) {
            DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
            expressionHandler.setRoleHierarchy(roleHierarchy);
            expressionHandler.setApplicationContext(context);
            return expressionHandler;
        }


    }
    

    // page no - 749, section  - 238.2.3
    @Configuration(proxyBeanMethods = false)
    @Profile({"authenticated-access","userdetails"})
    public static class PartA2_AuthenticatedAccess {

        /**
         * https://github.com/jzheaux/cve-2023-34035-mitigations
         * An explicit MvcRequestMatcher.Builder is necessary when mixing SpringMvc with
         * non-SpringMvc Servlets. Enabling the H2 console puts us in that position.
         * Dissabling (spring.h2.console.enabled=false) or being explicit as to which URI
         * apply to SpringMvc avoids the problem.
         * @param introspector
         * @return
         */
        @Bean
        MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
            return new MvcRequestMatcher.Builder(introspector);
        }

        @Bean
        public WebSecurityCustomizer  authzStaticResources(MvcRequestMatcher.Builder mvc){
            return web -> web.ignoring().requestMatchers(mvc.pattern("/content/**"));
        }
        @Bean
        @Order(0)
        public SecurityFilterChain  configure(HttpSecurity http) throws Exception  {
            // requirement -2
            http.httpBasic(Customizer.withDefaults());
            // form login disabled
            http.formLogin(cfg->cfg.disable());
            // Requirement - 3
            http.csrf(cfg -> cfg.disable());
            // Requirement - 4
            http.sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


            http.securityMatchers(cfg -> cfg.requestMatchers("/api/**") );


            /*
             * Start of
             * path based constraints to pass  extends A1_AnonymousAccessNTest config for anonymous-access
             */
            http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.HEAD).permitAll());  // Requirement: 3.b

            http.authorizeHttpRequests(cfg-> 
                    cfg.requestMatchers(HttpMethod.GET,"/api/autos","/api/autos/**","/api/autorentals","/api/autorentals/**").permitAll()
                     
            ); // Requirement 3.c

            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(HttpMethod.POST,"/api/autos/query").permitAll());  // Req- 3.d
            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(HttpMethod.POST,"/api/autorentals/query").permitAll());

            /*
             * End
             */

             /*
             * Start of
             * path based constraints to pass  extends A2_AuthenticatedAccessNTest config for authenticated-access
             */

            http.authorizeHttpRequests(cfg -> cfg.requestMatchers("/api/whoAmI").permitAll());

            http.authorizeHttpRequests(cfg -> cfg.requestMatchers("/api/autos","/api/autos/**").authenticated());
            http.authorizeHttpRequests(cfg -> cfg.requestMatchers("/api/renters","/api/renters/**").authenticated());
            http.authorizeHttpRequests(cfg -> cfg.requestMatchers("/api/autorentals","/api/autorentals/**").authenticated());
            
            

            return http.build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("nosecurity")
    public static class PartA2b_NoSecurity {
        /**
         * https://github.com/jzheaux/cve-2023-34035-mitigations
         * An explicit MvcRequestMatcher.Builder is necessary when mixing SpringMvc with
         * non-SpringMvc Servlets. Enabling the H2 console puts us in that position.
         * Dissabling (spring.h2.console.enabled=false) or being explicit as to which URI
         * apply to SpringMvc avoids the problem.
         * @param introspector
         * @return
         */
        @Bean
        MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
            return new MvcRequestMatcher.Builder(introspector);
        }

        @Bean
        public WebSecurityCustomizer authzStaticResources(MvcRequestMatcher.Builder mvc){
            return web -> web.ignoring().requestMatchers(mvc.pattern("/content/**"));
        }

        @Bean
        @Order(0)
        public SecurityFilterChain configure(HttpSecurity http) throws Exception {
            
            http.securityMatchers(cfg->cfg.requestMatchers("/api/**"));

            http.authorizeHttpRequests(cfg->cfg.requestMatchers("/api/**").permitAll());

            http.formLogin(cfg->cfg.disable());
            http.httpBasic(cfg->cfg.disable());
            http.csrf(cfg -> cfg.disable());
            return http.build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile({"nosecurity","userdetails","authorities", "authorization"})
    public static class PartA3_UserDetailsPart {

        /**
         * https://github.com/jzheaux/cve-2023-34035-mitigations
         * An explicit MvcRequestMatcher.Builder is necessary when mixing SpringMvc with
         * non-SpringMvc Servlets. Enabling the H2 console puts us in that position.
         * Dissabling (spring.h2.console.enabled=false) or being explicit as to which URI
         * apply to SpringMvc avoids the problem.
         * @param introspector
         * @return
         */
        // @Bean
        // MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector){
        //     return new MvcRequestMatcher.Builder(introspector);
        // }

        // @Bean
        // public WebSecurityCustomizer authzStaticResources(MvcRequestMatcher.Builder mvc) {
        //     return web -> web.ignoring().requestMatchers(mvc.pattern("/content/**"));
        // }

        @Bean
        public PasswordEncoder passwordEncoder()  {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(PasswordEncoder encoder, Accounts accounts){

            List<UserDetails> users = new ArrayList<>();
            for (AccountProperties acct : accounts.getAccounts()) {
                users.add(User.withUsername(acct.getUsername())
                            .passwordEncoder(encoder::encode)
                            .password(acct.getPassword())
                            .roles(acct.getAuthorities().toArray(new String[0]))
                            .build());
            }

            return new InMemoryUserDetailsManager(users);
            

        }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService uds) throws Exception {
            AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
            builder.userDetailsService(uds);
            return builder.build();
        }

    }
}

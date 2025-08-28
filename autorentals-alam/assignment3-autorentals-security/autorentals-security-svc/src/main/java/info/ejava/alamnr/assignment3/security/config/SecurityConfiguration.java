package info.ejava.alamnr.assignment3.security.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import info.ejava.assignments.security.autorenters.svc.AccountProperties;
import info.ejava.assignments.security.autorenters.svc.Accounts;
import lombok.extern.slf4j.Slf4j;
@Slf4j
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
        public SecurityFilterChain configureaAnonymousProfile(HttpSecurity http, MvcRequestMatcher.Builder mvc, RoleHierarchy roleHierarchy) throws Exception{

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
        public SecurityFilterChain  configureAuthenticatedAndUserDetailsProfile(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception  {
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
            
            log.info("*************************************** auth manager - {}", authenticationManager);
            http.authenticationManager(authenticationManager);
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
        public SecurityFilterChain configureNoSecurityProfile(HttpSecurity http) throws Exception {
            
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
         * @throws Exception 
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
        @Order(0)
        public SecurityFilterChain configureAnonymousHasNoAuthority(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

            http.securityMatchers(cfg->cfg.requestMatchers("/api/**"));
            // http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.GET,"/api/authorities")
            //                                     .hasAnyAuthority("ROLE_ADMIN","ROLE_MEMBER","ROLE_MGR","PROXY"));
            
            http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.GET,"/api/authorities").permitAll());
            http.authenticationManager(authenticationManager);
            http.formLogin(cfg->cfg.disable());
            http.sessionManagement(cfg->cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            http.httpBasic(Customizer.withDefaults());
            return http.build();
        }

        @Bean
        @Order(500)
        public SecurityFilterChain configureUserHasAuthorities(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

            http.securityMatchers(cfg->cfg.requestMatchers("/api/**"));
            http.authorizeHttpRequests(cfg->cfg.requestMatchers(HttpMethod.GET,"/api/authorities")
                                                .hasAnyAuthority("ROLE_ADMIN","ROLE_MEMBER","ROLE_MGR","PROXY"));
            
            
            http.authenticationManager(authenticationManager);
            http.formLogin(cfg->cfg.disable());
            http.sessionManagement(cfg->cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            http.httpBasic(Customizer.withDefaults());
            return http.build();
        }
        @Bean
        @Order(1000)
        public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
            MediaTypeRequestMatcher htmlRequestMatcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
            htmlRequestMatcher.setUseEquals(true);

            http.securityMatchers(cfg -> cfg.requestMatchers("/h2-console*","/h2-console/**")
                                            .requestMatchers("/login","/logout")
                                            .requestMatchers(RequestMatchers.allOf(
                                                htmlRequestMatcher, // only want to service HTML error pages
                                                AntPathRequestMatcher.antMatcher("/error")
                                            ))    );
            
            http.authorizeHttpRequests(cfg -> cfg.requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET,".+(.css|.jsp|.gif)$"))
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated());
            
            http.formLogin(cfg -> cfg.permitAll()
                                    .successForwardUrl("/h2-console"));
            
            http.csrf(cfg -> cfg.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")));
            http.headers(cfg -> cfg.frameOptions(fo -> fo.disable()));

            http.authenticationManager(authenticationManager);
            return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder()  {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsServiceInMemory(PasswordEncoder encoder, Accounts accounts){

            List<UserDetails> users = new ArrayList<>();
            for (AccountProperties acct : accounts.getAccounts()) {
                users.add(User.withUsername(acct.getUsername())
                            .passwordEncoder(encoder::encode)
                            .password(acct.getPassword())
                            .authorities(acct.getAuthorities().toArray(new String[0]))
                            .build());
            }

             return new InMemoryUserDetailsManager(users);
            

        }

        // @Bean
        // public UserDetailsService userDetailsServiceJdbc(DataSource userDataSource){

        //     JdbcDaoImpl jdbcUds = new JdbcDaoImpl();
        //     jdbcUds.setDataSource(userDataSource);

        //     return jdbcUds;
            

        // }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http, List<UserDetailsService> udsList) throws Exception {
            AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
            for (UserDetailsService userDetailsService : udsList) {
                builder.userDetailsService(userDetailsService);    
            }
            
            builder.parentAuthenticationManager(null); // prevent from being recursive
            return builder.build();
        }

    }

    @Configuration(proxyBeanMethods =  false)
    @Profile("authorization")
    // enable global method securityfor prePostEnabled

    public static class PartA_Authorizatonnn {

            @Bean
            public SecurityFilterChain configureAuthorizationProfile(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

                http.authenticationManager(authenticationManager);
                return http.build();
            }

            @Bean
            public RoleHierarchy roleHierarchy(){
                return RoleHierarchyImpl.withDefaultRolePrefix()
                        // todo
                        .build();
            }
    }
}

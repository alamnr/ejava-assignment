package info.ejava.alamnr.assignment3.security.config;

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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    @Configuration(proxyBeanMethods = false)
    @Profile("anonymous-access")
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
    
}

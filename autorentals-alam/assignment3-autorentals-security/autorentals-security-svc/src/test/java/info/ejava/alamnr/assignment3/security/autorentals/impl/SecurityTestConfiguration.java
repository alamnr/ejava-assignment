package info.ejava.alamnr.assignment3.security.autorentals.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import info.ejava.assignments.api.autorentals.svc.main.rental.impl.ApiImplNTestConfiguration;
import info.ejava.assignments.security.autorenters.svc.ProvidedAuthorizationTestHelperConfiguration;
import info.ejava.examples.common.web.RestTemplateConfig;
import info.ejava.examples.common.web.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.Apache5SslUtils;

@TestConfiguration(proxyBeanMethods = false)
@Import({//Configuration class you supplied to the API tests with the TestHelper
    ApiImplNTestConfiguration.class,
    //Security configuration provided with many authenticated/authorized beans
    ProvidedAuthorizationTestHelperConfiguration.class
})
@Slf4j
public class SecurityTestConfiguration {

    @Value("${spring.security.user.name:}")
    private String username;
    @Value("${spring.security.user.password:}")
    private String password;

    /*
     * Provides early test the literal name of the authenticated user
     * used by authnUser provided below
     */

    @Bean
    @ConditionalOnProperty(prefix = "spring.security.user", name={"name"})
    public String authnUsername(){
        return username;
    }

    /**
     * Provides tests with the literal name returned for an anonymous caller from
     * the whoAmI endpoint
     */
    @Bean
    public String anonymousUsername() {
        return "(null)";
    }

    /**
     * Provides tests a URL to the whoAmI resource that can identify the caller's identity
     */
    @Bean @Lazy
    public URI whoAmIUrl(ServerConfig serverConfig) {
        URI baseUrl = serverConfig.getBaseUrl();
        return UriComponentsBuilder.fromUri(baseUrl).path("/api/whoAmI").build().toUri();
    }

    /**
     * Provides early tests with a user with valid credentials
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.security.user", name={"name","password"})
    public RestTemplate authnUser(RestTemplateBuilder builder) {
        BasicAuthenticationInterceptor authn = new BasicAuthenticationInterceptor(username, password);
        return new RestTemplateConfig().restTemplateDebug(builder, authn);
    }

    /**
     * Provides tests with a user with a bad password
     */
    @Bean
    public RestTemplate badUser(RestTemplateBuilder builder) {
        BasicAuthenticationInterceptor authn = new BasicAuthenticationInterceptor(username, "badpass");
        return new RestTemplateConfig().restTemplateDebug(builder, authn);
    }

    /**
     * Creates a ClientHttpRequestFactory that optionally uses TLS based on the presence of
     * an SSLFactory.
     * @param sslFactory to trigger TLS handling
     * @return ClientHttpRequestFactory
     */
    @Bean 
    @ConditionalOnProperty(name = "https.enabled", havingValue = "true", matchIfMissing = false)
    public ClientHttpRequestFactory httpsRequestFactory(@Autowired(required = false) SSLFactory sslFactory) {
        PoolingHttpClientConnectionManagerBuilder builder = PoolingHttpClientConnectionManagerBuilder.create();
        PoolingHttpClientConnectionManager connectionManager = Optional.ofNullable(sslFactory)
                .map(sf->builder.setSSLSocketFactory(Apache5SslUtils.toSocketFactory(sf)))
                .orElse(builder)
                .build();

        HttpClient httpsClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpsClient);
    }

    /**
     * Conditional factory method that will provide an SSLFactory when the "it.server.trust-store"
     * property is present and not empty.
     * @param resourceLoader to load classpath resource
     * @param baseServerConfig properties pointing to trustStore
     * @return SSLFactory initialized with trustStore
     * @throws IOException if error reading keystore
     */
    @Bean
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${it.server.trust-store:}')")
    public SSLFactory sslFactory(ResourceLoader resourceLoader, ServerConfig baseServerConfig) throws IOException {
        try (InputStream trustStoreStream = resourceLoader.getResource(baseServerConfig.getTrustStore()).getInputStream()) {
            return SSLFactory.builder()
                    .withProtocols("TLSv1.2")
                    .withTrustMaterial(trustStoreStream, baseServerConfig.getTrustStorePassword())
                    .build();
        } catch (FileNotFoundException ex) {
            throw new IllegalStateException("unable to locate truststore: " + baseServerConfig.getTrustStore(), ex);
        }
    }




}

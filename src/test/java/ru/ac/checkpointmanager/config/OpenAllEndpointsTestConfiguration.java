package ru.ac.checkpointmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import ru.ac.checkpointmanager.security.filter.JwtAuthenticationFilter;

/*
 *Test configuration disable authorization checking for endpoints
 * */
@TestConfiguration
public class OpenAllEndpointsTestConfiguration {

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().anyRequest());
    }

    /**
     * Exclude jwt filter from chain
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> registrationBean(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

}

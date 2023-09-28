package ru.ac.checkpointmanager.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.ac.checkpointmanager.service.UserDetailServiceImpl;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailServiceImpl userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers("/", "/authentication/**").permitAll()
                                .requestMatchers("/welcome").authenticated()
                                .anyRequest().authenticated()
                ).formLogin(
                        form -> form
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .loginPage("/authentication/login")
                                .loginProcessingUrl("/authentication/login")
                                .defaultSuccessUrl("/welcome")
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                );
        return http.build();
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests((authorizeRequests) ->
//                        authorizeRequests
//                                  .requestMatchers("/admin/**").hasRole("ADMIN")
//                                .requestMatchers("/**").hasRole("USER")
//                                .requestMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs", "/webjars/**")
//                                .permitAll()
//                ).exceptionHandling(
//                        exception -> exception
//                                .authenticationEntryPoint(((request, response, authException) -> {
//                                    response.sendRedirect("http://localhost:8080/authentication/login.html");
//                                }))
//                );
//                .csrf(AbstractHttpConfigurer::disable)



    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userService);
        return authenticationProvider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}

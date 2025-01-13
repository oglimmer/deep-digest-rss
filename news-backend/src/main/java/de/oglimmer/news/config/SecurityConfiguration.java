package de.oglimmer.news.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(
            HttpSecurity http,
            @Value("${auth.write-user}") String writeUser,
            @Value("${auth.write-password}") String writePassword,
            @Value("${auth.actuator-user}") String actuatorUser,
            @Value("${auth.actuator-password}") String actuatorPassword,
            @Value("${auth.swagger-user}") String swaggerUser,
            @Value("${auth.swagger-password}") String swaggerPassword) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.parentAuthenticationManager(null);
        authenticationManagerBuilder.inMemoryAuthentication()
                .withUser(actuatorUser)
                .password(passwordEncoder().encode(actuatorPassword))
                .roles("actuator", "actuator-public", "doc")
                .and()
                .withUser(swaggerUser)
                .password(passwordEncoder().encode(swaggerPassword))
                .roles("doc")
                .and()
                .withUser(writeUser)
                .password(passwordEncoder().encode(writePassword))
                .roles("write");
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/actuator/**", "/actuator").hasRole("actuator-public")
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("doc")
                                .requestMatchers(POST, "/api/v1/news").hasRole("write")
                                .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

}

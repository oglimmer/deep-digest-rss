package de.oglimmer.news.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {

    private NewsConfiguration newsConfiguration;

    @Bean
    public UserDetailsService userDetailsService(@Value("${auth.write-user}") String writeUser,
                                                 @Value("${auth.write-password}") String writePassword,
                                                 @Value("${auth.actuator-user}") String actuatorUser,
                                                 @Value("${auth.actuator-password}") String actuatorPassword,
                                                 @Value("${auth.swagger-user}") String swaggerUser,
                                                 @Value("${auth.swagger-password}") String swaggerPassword,
                                                 PasswordEncoder passwordEncoder) {
        UserDetails read = User.builder()
                .username("read")
                .password(passwordEncoder.encode("read"))
                .roles("read")
                .build();
        UserDetails write = User.builder()
                .username(writeUser)
                .password(passwordEncoder.encode(writePassword))
                .roles("write", "read", "actuator-public", "actuator", "doc")
                .build();
        UserDetails acuator = User.builder()
                .username(actuatorUser)
                .password(passwordEncoder.encode(actuatorPassword))
                .roles("actuator-public", "actuator", "doc")
                .build();
        UserDetails swagger = User.builder()
                .username(swaggerUser)
                .password(passwordEncoder.encode(swaggerPassword))
                .roles("doc")
                .build();
        return new InMemoryUserDetailsManager(write, read, acuator, swagger);
    }


    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
        TokenBasedRememberMeServices.RememberMeTokenAlgorithm encodingAlgorithm = TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256;
        TokenBasedRememberMeServices rememberMe = new TokenBasedRememberMeServices(newsConfiguration.getTokenKey(), userDetailsService, encodingAlgorithm);
        rememberMe.setMatchingAlgorithm(TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256);
        rememberMe.setAlwaysRemember(true);
        return rememberMe;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {
        return http
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/actuator/**", "/actuator").hasRole("actuator-public")
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("doc")
                                .requestMatchers("/api/v1/feed-item-to-process").hasRole("write")
                                .requestMatchers(POST, "/api/v1/news").hasRole("write")
                                .requestMatchers(POST, "/api/v1/feed").hasRole("write")
                                .anyRequest().hasRole("read")
                )
                .rememberMe((remember) -> remember
                        .rememberMeServices(rememberMeServices)
                )
                .httpBasic(Customizer.withDefaults())
                .with(new QueryParamAuthConfigurer<>(), Customizer.withDefaults())
                .build();
    }

}

package com.example.UserRegistration.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
@EnableWebSecurity
@Configuration
public class SpringSecurity {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // allow static resources
                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()

                // existing app route rules
                .requestMatchers(new AntPathRequestMatcher("/notekeep")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/register/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/notes/**")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/editProfile")).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/notekeep")
                .loginProcessingUrl("/notekeep")
                .defaultSuccessUrl("/notes")
                .usernameParameter("email")
                .permitAll()
            )
            .logout(l -> l.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

package com.example.UserRegistration.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.example.UserRegistration.Security.OAuth2LoginSuccessHandler;
@EnableWebSecurity
@Configuration
public class SpringSecurity {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                           HandlerMappingIntrospector introspector,
                                           OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // allow static resources
                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/login/oauth2/**")).permitAll()

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
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/notekeep")
                .successHandler(oAuth2LoginSuccessHandler)
            )
            .logout(l -> l.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

package com.example.UserRegistration.Security;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.UserRegistration.Service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UserService userService;

    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
        setDefaultTargetUrl("/notes");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
            String email = attributes == null ? null : (String) attributes.get("email");

            if (!StringUtils.hasText(email)) {
                logger.warn("OAuth2 login succeeded but no email attribute was found for principal attributes={}", attributes);
                response.sendRedirect("/notekeep?oauthError");
                return;
            }

            String firstName = (String) attributes.getOrDefault("given_name", "");
            String lastName = (String) attributes.getOrDefault("family_name", "");
            userService.syncOAuthUser(email, firstName, lastName);
            logger.info("OAuth2 login linked to local user account {}", email);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}

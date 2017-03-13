package com.ugurhicyilmam.config;


import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.AuthService;
import com.ugurhicyilmam.service.exceptions.AccessTokenInvalidException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AccessFilter extends GenericFilterBean {

    private static final String accessTokenHeaderName = "Access-Token";
    private final AuthService authService;

    public AccessFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String accessToken = ((HttpServletRequest) servletRequest).getHeader(accessTokenHeaderName);
        UsernamePasswordAuthenticationToken authentication = createAuthByAccessToken(accessToken);

        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private UsernamePasswordAuthenticationToken createAuthByAccessToken(String accessToken) {
        UsernamePasswordAuthenticationToken authenticationToken = null;

        try {
            User user = authService.getUserByValidAccessToken(accessToken);
            authenticationToken = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        } catch (AccessTokenInvalidException ex) {
            // ignore exception
        }

        return authenticationToken;
    }

}

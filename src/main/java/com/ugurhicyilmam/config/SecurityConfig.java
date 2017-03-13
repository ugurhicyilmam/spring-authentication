package com.ugurhicyilmam.config;

import com.ugurhicyilmam.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private AuthService authService;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable() // disable basic authentication
                .csrf().disable() // don't need csrf since won't be using sessions
                .logout().disable() // disable default logout
                .formLogin().disable() // disable default login
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // disable sessions

        AccessFilter accessFilter = new AccessFilter(authService);
        http.addFilterBefore(accessFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}

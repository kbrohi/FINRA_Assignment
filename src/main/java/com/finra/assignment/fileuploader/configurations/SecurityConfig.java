package com.finra.assignment.fileuploader.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

@Configuration

//Activates spring basic web security
@EnableWebSecurity

/**
 * Configures spring basic security
 */
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * Setup the basic auth username and password
     * @param auth authentication manager
     * @throws Exception exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("user1").password("secret1").roles("USER");
    }

    //secure all the http endpoint exposed bu the spring controllers
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().fullyAuthenticated();
        http.httpBasic();
        http.csrf().disable();
    }

}

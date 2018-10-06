/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.boot.config;

import de.mc.ladon.server.core.api.exceptions.LadonIllegalArgumentException;
import de.mc.ladon.server.core.api.persistence.entities.User;
import de.mc.ladon.server.core.api.persistence.services.LadonUserDetailsManager;
import de.mc.ladon.server.core.persistence.entities.impl.LadonUser;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.stream.Collectors;

/**
 * WebSecurityConfig
 * Created by Ralf Ulrich on 11.12.15.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private LadonUserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/admin/feed/**")
                .permitAll()
                .antMatchers("/**")
                .permitAll()
                .antMatchers("/admin/assets/**")
                .permitAll()
                .antMatchers("/admin/cassandra/init")
                .permitAll()
                .antMatchers("/admin/health")
                .permitAll()
                .antMatchers("/admin/metrics")
                .permitAll()
                .antMatchers("/services/**")
                .permitAll()
                .antMatchers("/admin/**").hasRole("admin")
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/admin/login")
                .defaultSuccessUrl("/admin/overview")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/admin/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new Manager(userDetailsManager)).passwordEncoder(passwordEncoder);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    class Manager implements UserDetailsManager {

        private final LadonUserDetailsManager manager;

        public Manager(LadonUserDetailsManager detailsManager) {
            manager = detailsManager;
        }

        @Override
        public void createUser(UserDetails userDetails) {
            manager.createUser(getLadonUser(userDetails));
        }

        @Override
        public void updateUser(UserDetails userDetails) {
            throw new NotImplementedException();
        }

        @Override
        public void deleteUser(String id) {
            manager.deleteUser(id);
        }

        @Override
        public void changePassword(String oldPW, String newPW) {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            manager.changePassword(name, oldPW, newPW);
        }

        @Override
        public boolean userExists(String id) {
            try {
                manager.loadUserByUsername(id);
                return true;
            } catch (LadonIllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
            try {
                return getSpringUser(manager.loadUserByUsername(s));
            } catch (LadonIllegalArgumentException e) {
                throw new UsernameNotFoundException(e.getMessage());
            }
        }
    }

    private User getLadonUser(org.springframework.security.core.userdetails.UserDetails u) {
        return new LadonUser(u.getUsername(), u.getPassword(), u.isEnabled(),
                u.getAuthorities()
                        .stream()
                        .map((grantedAuthority) -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                        .collect(Collectors.toSet()));
    }

    private org.springframework.security.core.userdetails.UserDetails getSpringUser(User u) {
        return new org.springframework.security.core.userdetails.User(u.getName(),
                u.getPassword(),
                u.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList()));
    }
}


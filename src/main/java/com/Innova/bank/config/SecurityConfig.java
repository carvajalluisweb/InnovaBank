package com.Innova.bank.config;

import com.Innova.bank.auth.security.CustomUserDetailsService;
import com.Innova.bank.auth.security.JwtAuthenticationFilter;
import com.Innova.bank.enums.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/auth/actualSession").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/users")
                        .hasAnyAuthority(Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.GET, "/api/users/*")
                        .hasAnyAuthority(Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/role")
                        .hasAuthority(Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/status")
                        .hasAuthority(Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.GET, "/api/accounts/me")
                        .hasAuthority(Rol.ROLE_USER.name())

                        .requestMatchers(HttpMethod.GET, "/api/accounts/me/*")
                        .hasAuthority(Rol.ROLE_USER.name())

                        .requestMatchers(HttpMethod.GET, "/api/accounts/allAccounts")
                        .hasAnyAuthority(Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.GET, "/api/accounts/*")
                        .hasAnyAuthority(Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.POST, "/api/accounts/createAccount")
                        .hasAnyAuthority(Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.PATCH, "/api/accounts/*/status")
                        .hasAuthority(Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.POST, "/api/transactions/transfer")
                        .hasAnyAuthority(Rol.ROLE_USER.name(), Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.POST, "/api/transactions/deposit")
                        .hasAnyAuthority(Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.POST, "/api/transactions/withdrawal")
                        .hasAnyAuthority(Rol.ROLE_USER.name(), Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .requestMatchers(HttpMethod.GET, "/api/transactions/me")
                        .hasAuthority(Rol.ROLE_USER.name())

                        .requestMatchers(HttpMethod.GET, "/api/transactions/account/*")
                        .hasAnyAuthority(Rol.ROLE_USER.name(), Rol.ROLE_OPERATOR.name(), Rol.ROLE_ADMIN.name())

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
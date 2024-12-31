package org.url.urlshortenerbe.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SecurityConfiguration {
    private static final String API_PREFIX = "/api";

    private final CustomJwtDecoder customJwtDecoder;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final String[] POST_PUBLIC_ENDPOINTS = {
        // Allows to create user (register) for everyone
        API_PREFIX + "/users",

        // Allow login (get token) and introspect token
        API_PREFIX + "/auth/**",

        // Allow url origin
        API_PREFIX + "/urls"
    };

    private final String[] GET_PUBLIC_ENDPOINTS = {
        API_PREFIX + "/auth/me", "/{hash}",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(auth -> auth
                // Allow POST public endpoints
                .requestMatchers(HttpMethod.POST, POST_PUBLIC_ENDPOINTS)
                .permitAll()

                // Allow GET public endpoints accessible to everyone
                .requestMatchers(HttpMethod.GET, GET_PUBLIC_ENDPOINTS)
                .permitAll()

                // /users endpoints
                .requestMatchers(HttpMethod.GET, "/users")
                .hasAuthority("MANAGE_USER")
                .requestMatchers(HttpMethod.PUT, "/users")
                .hasAuthority("MANAGE_USER")
                .requestMatchers(HttpMethod.DELETE, "/users")
                .hasAuthority("MANAGE_USER")

                // /roles endpoints
                .requestMatchers(HttpMethod.POST, "/roles")
                .hasAuthority("MANAGE_ROLE")
                .requestMatchers(HttpMethod.GET, "/roles")
                .hasAuthority("MANAGE_ROLE")
                .requestMatchers(HttpMethod.PUT, "/roles")
                .hasAuthority("MANAGE_ROLE")
                .requestMatchers(HttpMethod.DELETE, "/roles")
                .hasAuthority("MANAGE_ROLE")

                // /permissions endpoints
                .requestMatchers(HttpMethod.POST, "/permissions")
                .hasAuthority("MANAGE_PERMISSION")
                .requestMatchers(HttpMethod.GET, "/permissions")
                .hasAuthority("MANAGE_PERMISSION")
                .requestMatchers(HttpMethod.PUT, "/permissions")
                .hasAuthority("MANAGE_PERMISSION")
                .requestMatchers(HttpMethod.DELETE, "/permissions")
                .hasAuthority("MANAGE_PERMISSION")

                // /urls endpoints
                // The POST at /urls is already allowed in the list GET_PUBLIC_ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/urls")
                .hasAuthority("MANAGE_URL")
                .requestMatchers(HttpMethod.PUT, "/urls")
                .hasAuthority("MANAGE_URL")
                .requestMatchers(HttpMethod.DELETE, "/urls")
                .hasAuthority("MANAGE_URL")

                // /campaigns endpoints
                .requestMatchers(HttpMethod.GET, "/campaigns")
                .hasAuthority("MANAGE_CAMPAIGN")
                // todo: implement put and delete endpoints too
                // todo: moreover implement soft delete on /users, /urls, /campaigns

                // business logic
                // /users/{userId}/urls for normal user, manager and admin
                .requestMatchers(HttpMethod.POST, "/users/{userId}/urls")
                .hasAuthority("CREATE_URL")
                .requestMatchers(HttpMethod.GET, "/users/{userId}/urls")
                .hasAuthority("READ_URL")
                .requestMatchers(HttpMethod.PUT, "/users/{userId}/urls")
                .hasAuthority("UPDATE_URL")
                .requestMatchers(HttpMethod.DELETE, "/users/{userId}/urls")
                .hasAuthority("DELETE_URL")

                // /users/{userId}/campaigns/{campaignId}/urls for manager and admin
                .requestMatchers(
                        HttpMethod.POST, "/users/{userId}/campaigns/", "/users/{userId}/campaigns/{campaignId}/urls")
                .hasAuthority("CREATE_CAMPAIGNS")
                .requestMatchers(
                        HttpMethod.GET, "/users/{userId}/campaigns/", "/users/{userId}/campaigns/{campaignId}/urls")
                .hasAuthority("READ_CAMPAIGNS")
                .requestMatchers(
                        HttpMethod.PUT, "/users/{userId}/campaigns/", "/users/{userId}/campaigns/{campaignId}/urls")
                .hasAuthority("UPDATE_CAMPAIGNS")
                .requestMatchers(
                        HttpMethod.DELETE, "/users/{userId}/campaigns/", "/users/{userId}/campaigns/{campaignId}/urls")
                .hasAuthority("DELETE_CAMPAIGNS")

                // any other requests must be authenticated
                .anyRequest()
                .authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        // Override JwtAuthenticationConverter
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                // Override JwtAuthenticationEntryPoint
                .authenticationEntryPoint(jwtAuthenticationEntryPoint));

        // Turn off csrf for development
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        // Handle AccessDeniedHandler
        httpSecurity.exceptionHandling(exception -> exception.accessDeniedHandler(new CustomAccessDeniedHandler()));

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}

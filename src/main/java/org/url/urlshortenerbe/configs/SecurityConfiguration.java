package org.url.urlshortenerbe.configs;

import java.util.List;

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
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
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

    private final String[] GET_PUBLIC_ENDPOINTS = {API_PREFIX + "/auth/me", "/{hash}"};

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
                .requestMatchers(HttpMethod.GET, API_PREFIX + "/users")
                .hasAuthority("MANAGE_USER")
                .requestMatchers(HttpMethod.PUT, API_PREFIX + "/users")
                .hasAnyAuthority("UPDATE_USER", "MANAGE_USER")
                .requestMatchers(HttpMethod.DELETE, API_PREFIX + "/users")
                .hasAuthority("MANAGE_USER")

                // /roles endpoints
                .requestMatchers(HttpMethod.POST, API_PREFIX + "/roles")
                .hasAuthority("MANAGE_ROLE")
                .requestMatchers(HttpMethod.GET, API_PREFIX + "/roles")
                .hasAuthority("MANAGE_ROLE")
                .requestMatchers(HttpMethod.PUT, API_PREFIX + "/roles")
                .hasAuthority("MANAGE_ROLE")
                .requestMatchers(HttpMethod.DELETE, API_PREFIX + "/roles")
                .hasAuthority("MANAGE_ROLE")

                // /permissions endpoints
                .requestMatchers(HttpMethod.POST, API_PREFIX + "/permissions")
                .hasAuthority("MANAGE_PERMISSION")
                .requestMatchers(HttpMethod.GET, API_PREFIX + "/permissions")
                .hasAuthority("MANAGE_PERMISSION")
                .requestMatchers(HttpMethod.PUT, API_PREFIX + "/permissions")
                .hasAuthority("MANAGE_PERMISSION")
                .requestMatchers(HttpMethod.DELETE, API_PREFIX + "/permissions")
                .hasAuthority("MANAGE_PERMISSION")

                // /urls endpoints
                // The POST at /urls is already allowed in the list GET_PUBLIC_ENDPOINTS
                .requestMatchers(HttpMethod.GET, API_PREFIX + "/urls/**")
                .hasAuthority("MANAGE_URL")
                .requestMatchers(HttpMethod.PUT, API_PREFIX + "/urls")
                .hasAuthority("MANAGE_URL")
                .requestMatchers(HttpMethod.DELETE, API_PREFIX + "/urls")
                .hasAuthority("MANAGE_URL")

                // /campaigns endpoints
                .requestMatchers(HttpMethod.GET, API_PREFIX + "/campaigns")
                .hasAuthority("MANAGE_CAMPAIGN")
                // todo: implement put and delete endpoints too
                // todo: moreover implement soft delete on /users, /urls, /campaigns

                // business logic
                // /users/{userId}/urls for normal user, manager and admin
                .requestMatchers(HttpMethod.POST, API_PREFIX + "/users/{userId}/urls")
                .hasAnyAuthority("CREATE_URL", "MANAGE_URL")
                .requestMatchers(HttpMethod.GET, API_PREFIX + "/users/{userId}/url/**")
                .hasAnyAuthority("READ_URL", "MANAGE_URL")
                .requestMatchers(HttpMethod.PUT, API_PREFIX + "/users/{userId}/urls")
                .hasAnyAuthority("UPDATE_URL", "MANAGE_URL")
                .requestMatchers(HttpMethod.DELETE, API_PREFIX + "/users/{userId}/urls")
                .hasAnyAuthority("DELETE_URL", "MANAGE_URL")

                // /users/{userId}/campaigns/{campaignId}/urls for manager and admin
                .requestMatchers(
                        HttpMethod.POST,
                        API_PREFIX + "/users/{userId}/campaigns",
                        API_PREFIX + "/users/{userId}/campaigns/{campaignId}/urls")
                .hasAnyAuthority("CREATE_CAMPAIGN", "MANAGE_CAMPAIGN")
                .requestMatchers(
                        HttpMethod.GET,
                        API_PREFIX + "/users/{userId}/campaigns/**",
                        API_PREFIX + "/users/{userId}/campaigns/{campaignId}/urls/**",
                        // Where to get the stats of url within a campaign
                        API_PREFIX + "/users/{userId}/campaigns/{campaignId}/urls/stats")
                .hasAnyAuthority("READ_CAMPAIGN", "MANAGE_CAMPAIGN")
                .requestMatchers(
                        HttpMethod.PUT,
                        API_PREFIX + "/users/{userId}/campaigns",
                        API_PREFIX + "/users/{userId}/campaigns/{campaignId}/urls")
                .hasAnyAuthority("UPDATE_CAMPAIGN", "MANAGE_CAMPAIGN")
                .requestMatchers(
                        HttpMethod.DELETE,
                        API_PREFIX + "/users/{userId}/campaigns",
                        API_PREFIX + "/users/{userId}/campaigns/{campaignId}/urls")
                .hasAnyAuthority("DELETE_CAMPAIGN", "MANAGE_CAMPAIGN")

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

        // Cors config
        httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource()));

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Specify your frontend origin
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

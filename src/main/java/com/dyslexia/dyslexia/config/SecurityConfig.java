package com.dyslexia.dyslexia.config;

import com.dyslexia.dyslexia.config.oauth.OAuth2SuccessHandler;
import com.dyslexia.dyslexia.config.oauth.CustomOAuth2UserService;
import com.dyslexia.dyslexia.filter.JwtAuthenticationFilter;
import com.dyslexia.dyslexia.util.JwtTokenProvider;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())).sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                authorize -> authorize.requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                        "/swagger-ui.html").permitAll()
                    .requestMatchers("/", "/oauth2/**", "/login/**", "/css/**", "/js/**")
                    .permitAll().requestMatchers("/users/signup").permitAll().anyRequest()
                    .permitAll()
//                .requestMatchers("/students/**").hasRole("STUDENT")
//                .requestMatchers("/teachers/**").hasRole("TEACHER")
//                .anyRequest().authenticated()
                //테스트할 땐 인증 없이 할 수 있도록 잠시 주석처리 해둘게요
            ).oauth2Login(oauth2 -> oauth2.userInfoEndpoint(
                    userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler));

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
            UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
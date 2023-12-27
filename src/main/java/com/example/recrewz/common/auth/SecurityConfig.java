package com.example.recrewz.common.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity//스프링 시큐리티 필터가 스프링 필터체인에 등록
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JWTProvider jwtProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean//인증 요청 처리
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)//csrf(사이트 간 위조 요청): http tag를 통한 공격
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)//Anthorization에 id와 pwd를 담는 방식으로 확장성은 좋지만 암호화가 되지 않은 채로 노출됨.
                //사용자 인증 전 토큰 검사
                //JWTAuthenticationFilter: 요청에서 JWT를 추출하고 이를 바탕으로 user를 인증처리(jwt를 가지고 있는 유저를 인증처리)
                //UsernamePasswordAuthenticationFilter: username과 passwrod로 인증(첫 로그인 시 유저를 확인하고 jwt지급)
                //인증을 처리하는 기본 필터 UserFilter 대신 커스텀 필터를 생성해서 등록할 수도 있다.
                .addFilterBefore(new JWTAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authorizeRequest) -> authorizeRequest
                        .requestMatchers("/api/member/**", "/api/moim/**", "/api/somoim/**", "/api/review/**").permitAll()
                        .requestMatchers("/auth/member/**", "/auth/moim/**", "/auth/somoim/**", "/auth/review/**", "/auth/photo/**").authenticated()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}

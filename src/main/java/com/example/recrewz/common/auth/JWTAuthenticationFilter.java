package com.example.recrewz.common.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTAuthenticationFilter extends GenericFilterBean {//JWT 인증 처리
    private final JWTProvider jwtProvider;

    @Override//필터 체인을 통과할 때마다 호출되는 메서드
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        /* CORS 헤더 설정 */
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");//도메인의 리소스에 접근할 수 있는 외부 도메인 지정
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "*");//허용하고 싶은 HTTP 방식 지정
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");//특정 요청에 이용할 수 있는 헤더에 제한 추가
        response.setStatus(HttpServletResponse.SC_OK);

        //헤더로 토큰 받기
        String token = jwtProvider.resolveToken((HttpServletRequest)servletRequest);
        //토큰 유효성 확인
        if (token != null && jwtProvider.validateToken(token)) {
            //토큰 검증(인증 객체 생성)
            Authentication authentication = jwtProvider.getAuthentication(token);
            //SecuritycontextHolder: 보안 컨텍스트 정보 저장 및 검색
            //보안 컨텍스트에 포함되는 정보: 인증된 사용자에 대한 인증 객체 및 권한 정보(아이디, 비밀번호 등)
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Security Context에 인증 정보를 저장했습니다. URI:" + ((HttpServletRequest)servletRequest).getServletPath());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}

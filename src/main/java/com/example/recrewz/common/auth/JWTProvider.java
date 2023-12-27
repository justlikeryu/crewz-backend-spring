package com.example.recrewz.common.auth;

import com.example.recrewz.member.MemberDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JWTProvider {

    private final Key key = Keys.hmacShaKeyFor("qwerqwersdfdsdgfsdgsdgfsfgsgafFSGHDFHJGFJGDFHSFGHSGFGDHFGJFGHJGFHJFHSFHDFGJHGJSFHSDHDHJFGHJGFHSFHGSFGHDFfgsdf".getBytes(StandardCharsets.UTF_8));
    private final PrincipalDetailsService principalDetailsService;

    //인증로직 => UserDetailsService(DB에서 회원 정보 획득) => User 정보 확인
    //=> 결과를 AuthenticationFilter에 전달 및 세부정보는 SecurityContext에 저장
    //jwt: header + payload(Claims) + signature
    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();

        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());

        return header;
    }

    private Map<String, Object> createClaim(MemberDTO dto) {
        Map<String, Object> claim = new HashMap<>();

        claim.put("username", dto.getId());

        return claim;
    }

    public String createJWT(MemberDTO dto) {
        Date now = new Date();

        // 액세스 토큰, 리프레쉬 토큰
        return Jwts.builder()
                .setSubject(dto.getId())
                .setHeader(createHeader())//헤더 등록
                .setClaims(createClaim(dto))//payload - claim 등록
                .setIssuedAt(now)//payload - 생성시간 등록
                .setExpiration(new Date(now.getTime() + Duration.ofHours(1).toMillis()))//payload-만료시간. 액세스 토큰
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    private Claims getClaim(String token) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }

    public String getUsernameFromToken(String token) {
        return (String) getClaim(token).get("username");
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
            System.out.println("유효하지 않은 토큰입니다.");
        } catch (ExpiredJwtException e) {
            System.out.println("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("잘못된 토큰입니다.");
        }
        return false;
    }

    //SecurityContextHolder에 저장할 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = principalDetailsService.loadUserByUsername(this.getUsernameFromToken(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}

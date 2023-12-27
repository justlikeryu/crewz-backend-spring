package com.example.recrewz.common.auth;

import com.example.recrewz.member.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

//UserDetails: 회원 정보를 담는 역할
//UserDetails객체를 상속받으면 스프링 시큐리티의 세션 저장소에 저장할 수 있다.
public class PrincipalDetails implements UserDetails {

    private final Member member;

    public PrincipalDetails(Member crew) {
        this.member = crew;
    }

    @Override//회원의 권한 리턴 -> 관한 관련 작업 수행
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("MEMBER"));

        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPwd();
    }

    @Override
    public String getUsername() {
        return member.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override//휴면계정
    public boolean isEnabled() {
        return true;
    }
}

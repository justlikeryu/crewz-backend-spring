package com.example.recrewz.common.auth;

import com.example.recrewz.member.Member;
import com.example.recrewz.member.MemberDTO;
import com.example.recrewz.member.MemberDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//인증 시 DB에서 회원을 찾고 UserDetails로 반환하는 loadUserByUsername메서드를 갖는다.
//로그인 요청이 오면 UserDetailsServicedml loadUserByUserName함수 자동 실행
@Service
public class PrincipalDetailsService implements UserDetailsService {
    @Autowired
    private MemberDao dao;

    //시큐리티 세션 = Authentication(내부 UserDetails)
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        //클라이언트에게서 받은 username으로 검색. 비밀번호는 PasswordEncoder가 처리하고 차후에 확인하는 로직 발생.
        //이 때 검색한 username으로 UserDetail 반환
        Member member = dao.select(id);

        return new PrincipalDetails(member);//시큐리티에서 사용할 PrincipalDetails타입으로 Authentication 객체 생성
    }
}
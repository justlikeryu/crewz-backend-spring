package com.example.recrewz.member;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private MemberDao dao;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setMemberDao(MemberDao dao) {
        this.dao = dao;
    }

    public MemberDTO getMember(String id, String pwd) {
        MemberDTO dto = null;
        Member member;

        try {
            dto = get(id);
            if(passwordEncoder.matches(pwd, dto.getPwd())) {
//            member = dao.selectByMember(id, passwordEncoder.encode(pwd));
            } else {
                dto = null;
            }
        } catch(DataAccessException e) {
            exceptionMessage(e, "getMember");
        }

        return dto;
    }

    public MemberDTO join(MemberDTO dto) {
        MemberDTO item = null;

        try {
            dao.insert(new Member(dto.getId(), passwordEncoder.encode(dto.getPwd()), dto.getName(), dto.getBirth(), dto.getTel(), dto.getPhoto(), dto.getSite()));

            item = get(dto.getId());
        } catch(DataAccessException e) {
            exceptionMessage(e, "join");
        }

        return item;
    }

    public MemberDTO get(String id) {
        Member member;
        MemberDTO dto = null;

        try {
            member = dao.select(id);
            dto = new MemberDTO(member.getId(), member.getPwd(), member.getName(), member.getBirth(), member.getTel(), member.getPhoto(), member.getSite());
        } catch(DataAccessException e) {
            exceptionMessage(e, "get");
        }

        return dto;
    }

    public MemberDTO edit(MemberDTO item, boolean pwdCh, boolean telCh) {
        MemberDTO dto = null;

        try {
            if(pwdCh && telCh)
                dao.updatePwdAndTel(new Member(item.getId(), passwordEncoder.encode(item.getPwd()), item.getName(), item.getBirth(), item.getTel(), item.getPhoto(), item.getSite()));
            else if(pwdCh)
                dao.updatePwd(new Member(item.getId(), passwordEncoder.encode(item.getPwd()), item.getName(), item.getBirth(), item.getTel(), item.getPhoto(), item.getSite()));
            else if(telCh)
                dao.updateTel(new Member(item.getId(), null, item.getName(), item.getBirth(), item.getTel(), item.getPhoto(), item.getSite()));
            dto = get(item.getId());
        } catch(DataAccessException e) {
            exceptionMessage(e, "edit");
        }

        return dto;
    }

    public void delete(String id) {
        dao.delete(id);
    }

    /**
     * 중복 검사 / 0 - 가능, 1 - 중복
     * @param id
     * @return
     */
    public int countById(String id) {
        return dao.selectById(id);
    }

    public void editProfile(String id, String photo) {
        dao.updateProfile(id, photo);
    }

    public String findIdByNameNTel(String name, String tel) {
        return dao.selectIdByNameNTel(name, tel);
    }

    public String findPwdByIdNTel(String id, String tel) {
        return dao.selectPwdByIdNTel(id, tel);
    }

    /**
     * 에러 메세지 추출 메서드
     * @param e 에러 추출 파라미터
     */
    private void exceptionMessage(DataAccessException e, String methodName) {
        System.out.println("MemberService::" + methodName + " -> " + e.getMessage());
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}

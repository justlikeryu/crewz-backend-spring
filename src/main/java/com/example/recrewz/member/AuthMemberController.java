package com.example.recrewz.member;

import com.example.recrewz.common.file.SftpUtils;
import com.example.recrewz.common.info.Info;
import com.example.recrewz.common.user.UserPool;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth/member")
public class AuthMemberController {
    private MemberService memberService;
    private SftpUtils sftpUtils;
    private UserPool userPool;
    private final Map<String, Object> map = new WeakHashMap<>();

    @Value("${spring.servlet.multipart.location}")
    private String path;

    /**
     *
     * @param id
     * @param request
     * @return
     */
    @DeleteMapping("/del")
    public Map<String, Object> del(@RequestHeader("Authorization") String token, @RequestParam String id, HttpServletRequest request) {
        map.clear();

        int msg = (int)userPool.delUser(id, request).get("msg");

        if(msg == 1) {
            MemberDTO dto = memberService.get(id);
            sftpUtils.connection();
            boolean exist = sftpUtils.exists(Info.path + "member" + File.separator + id);
            if(exist) {
                boolean del = sftpUtils.delete(Info.path + "member" + File.separator + id, dto.getPhoto());
                if(del) {
                    System.out.println("회원 폴더 삭제 완료");
                }
            }
            memberService.delete(id);
            map.put("msg", "회원 삭제 완료");
        }

        sftpUtils.disconnection();

        return map;
    }

    /**
     * 개인정보 확인
     * @param id 회원 아이디
     * @return 개인정보
     */
    @PostMapping("/info/{id}")
    public Map<String, Object> getMember(@RequestHeader("Authorization") String token, @PathVariable("id") String id) {
        map.clear();

        MemberDTO member = memberService.get(id);
        if(member != null) {
            map.put("flag", true);
            map.put("member", member);
        } else {
            map.put("flag", false);
        }

        return map;
    }

    /**
     * 내 정보 수정(프로필 사진 변경x)
     * @param dto 수정하려는 내 정보
     * @return 수정된 내 정보
     */
    @PutMapping("/edit/info")
    public Map<String, Object> editMemberInfo(@RequestHeader("Authorization") String token, MemberDTO dto){
        map.clear();

        System.out.println("DTO: " + dto);

        MemberDTO empty = memberService.get(dto.getId());
        boolean pwdCh = false;
        boolean telCh = false;
        if(!dto.getPwd().equals(empty.getPwd())) {
            empty.setPwd(dto.getPwd());
            pwdCh = true;
        }
        if(!dto.getTel().equals(empty.getTel())) {
            empty.setTel(dto.getTel());
            telCh = true;
        }

        MemberDTO item = memberService.edit(empty, pwdCh, telCh);
        if(item != null) {
            map.put("flag", true);
            map.put("member", item);
        }

        return map;
    }

    @PutMapping("/edit/profile")
    public Map<String, Object> editMemberProfile(@RequestHeader("Authorization") String token, String id, MultipartFile mf) {
        map.clear();
        boolean flag = false;

        MemberDTO empty = memberService.get(id);

        sftpUtils.connection();
        boolean preProfile = sftpUtils.deleteProfile(Info.path + "member" + File.separator + id, empty.getPhoto());
        if(preProfile) {
            boolean profileUpload = sftpUtils.upload(Info.path + "member" + File.separator + id, mf);
            if(profileUpload) {
                memberService.editProfile(id, mf.getOriginalFilename());
                flag = true;
            }
        }

        map.put("flag", flag);

        return map;
    }

    @Autowired
    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }

    @Autowired
    public void setSftpUtils(SftpUtils sftpUtils) {
        this.sftpUtils = sftpUtils;
    }

    @Autowired
    public void setUserPool(UserPool userPool) {
        this.userPool = userPool;
    }
}

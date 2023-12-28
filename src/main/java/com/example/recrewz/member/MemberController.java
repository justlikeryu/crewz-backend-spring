package com.example.recrewz.member;

import com.example.recrewz.common.auth.JWTProvider;
import com.example.recrewz.common.file.SftpUtils;
import com.example.recrewz.common.info.Info;
import com.example.recrewz.common.user.UserPool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/member")
public class MemberController {
    private MemberService memberService;
    private SftpUtils sftpUtils;
    private UserPool userPool;
    private JWTProvider jwtProvider;
    private AuthenticationManagerBuilder authenticationManagerBuilder;
    private final Map<String, Object> map = new WeakHashMap<>();

    @Value("${spring.servlet.multipart.location}")
    private String path;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam("id") String id, @RequestParam("pwd") String pwd, HttpServletRequest request) {
        map.clear();

        MemberDTO dto = memberService.getMember(id, pwd);
        if(dto != null) {
            int msg = (int)userPool.setUser(dto, request).get("msg");
            if(msg == -1) {
                map.put("flag", false);
                map.put("msg", "이미 로그인 된 회원입니다.");

                return map;
            }

            // 토큰 생성
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(id, pwd);

            // authenticationManager: 인증 관리
            // 생성된 User토큰이 인증 메서드로 전달되어 authenticationManager는 해당 토큰으로 사용자 인증
            // 사용자가 검증되면 Authentication객체 생성ㄴ
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(usernamePasswordAuthenticationToken);

            boolean check = authentication.isAuthenticated();
            if(check) {
                // 토큰 발행
                String token = jwtProvider.createJWT(new MemberDTO(id, "", "", null, "", "", ""));
                map.put("flag", true);
                map.put("token", token);
            }

            return map;
        }

        map.put("flag", false);
        map.put("msg", "아이디 혹은 비밀번호를 잘못 입력하셨습니다.");

        return map;
    }

    /**
     * 로그아웃
     * @param id 회원 아이디
     * @return 회원 로그아웃 여부 값 리턴
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestParam("id") String id, HttpServletRequest request){
        map.clear();

        int msg = (int)userPool.delUser(id, request).get("msg");

        if(msg == 1) {
            map.put("msg", "로그아웃 성공!");
        } else {
            map.put("msg", "이미 로그아웃 했거나 잘못된 접근 방법입니다.");
        }

        return map;
    }

    /**
     *
     * @param id
     * @param request
     * @return
     */
    @DeleteMapping("/auth/del")
    public Map<String, Object> del(@RequestHeader("token") String token, @RequestParam String id, HttpServletRequest request) {
        map.clear();

        int msg = (int)userPool.delUser(id, request).get("msg");

        if(msg == 1) {
            MemberDTO dto = memberService.get(id);
//            sftpUtils.init(Info.host, Info.userName, Info.password, Info.port, null);
            sftpConnect();
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

        sftpDisconnection();

        return map;
    }

    /**
     * 회원가입(photo: default.png 설정되어 있음)
     * @param m 회원정보
     * @return 회원가입 여부
     */
    @PostMapping("/join")
    public Map<String, Object> join(MemberDTO dto) {
        map.clear();

        if(dto.getId() != null && dto.getPwd() != null && dto.getName() != null && dto.getTel() != null && dto.getBirth() != null && dto.getSite() !=null) {
//            sftpUtils.init(Info.host, Info.userName, Info.password, Info.port, null);
            sftpConnect();
            // 회원 중복 여부 확인
            boolean exist = sftpUtils.exists(Info.path + "member" + File.separator + dto.getId());
            if(!exist) {
                // 회원 폴더 생성
                boolean dir = sftpUtils.memberDir(Info.path + "member" + File.separator + dto.getId());
                if (dir) {
                    // 회원 폴더에 기본 프로필 이미지 저장
                    boolean move = sftpUtils.moveProfile("member", dto.getId());
                    // 데이터베이스에 회원 정보 저장
                    if (move) {
                        dto.setPhoto("default.png");
                        memberService.join(dto);
                    }
                }
            } else {
                System.out.println("이미 존재하는 파일!");
                sftpUtils.disconnection();

                map.put("flag", false);
                map.put("message", "이미 가입된 회원입니다.");

                return map;
            }
        }

        map.put("flag", true);
        map.put("message", "가입을 축하드립니다!");
        sftpDisconnection();

        return map;
    }

    /**
     * 아이디 찾기
     * @param name 이름
     * @param tel 전화번호
     * @return 아이디
     */
    @PostMapping("/find/id")
    public Map<String, Object> findId(@RequestParam String name, @RequestParam String tel) {
        map.clear();

        String result = memberService.findIdByNameNTel(name, tel);
        if (result == null) {
            result = "null";
        }

        map.put("result", result);

        return map;
    }

    /**
     * 비밀번호 찾기
     * @param id 아이디
     * @param tel 전화번호
     * @return 비밀번호
     */
    @PostMapping("/find/pwd")
    public Map<String, Object> findPwd(@RequestParam String id, @RequestParam String tel) {
        map.clear();

        String result = memberService.findPwdByIdNTel(id, tel);
        if (result == null) {
            result = "null";
        }

        map.put("result", result);

        return map;
    }

    /**
     * 이미지 처리
     * @param id 이미지 처리를 위한 파라미터
     * @return 이미지 표시
     */
    @GetMapping("/img")
    public synchronized ResponseEntity<byte[]> getImg(@PathParam("id") String id) {
//        map.clear();
        sftpUtils.connection();

        MemberDTO dto = memberService.get(id);
        System.out.println("Member Img: " + dto);
        byte[] bytes = sftpUtils.downloadImg(Info.path + "member" + File.separator + id, dto.getPhoto());

        String[] ext = dto.getPhoto().split("\\.");

        HttpHeaders header = new HttpHeaders();

        header.add("Content-Type", "image/" + ext[1]);

        sftpUtils.disconnection();

        return new ResponseEntity<>(bytes, header, HttpStatus.OK);
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

    @Autowired
    public void setJwtProvider(JWTProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Autowired
    public void setAuthenticationManagerBuilder(AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    private void sftpConnect() {
        sftpUtils.init(Info.host, Info.userName, Info.password, Info.port, null);
    }

    private void sftpDisconnection() {
        sftpUtils.disconnection();
    }
}

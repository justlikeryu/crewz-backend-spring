package com.example.recrewz.moim;

import com.example.recrewz.common.file.SftpUtils;
import com.example.recrewz.common.user.UserPool;
import com.example.recrewz.member.MoimFormDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

@RestController
@CrossOrigin("*")
@RequestMapping("/auth/moim")
public class AuthMoimController {
    private MoimService moimService;
    private SftpUtils sftpUtils;
    private UserPool userPool;
    private final Map<String, Object> map = new WeakHashMap<>();

    /**
     * 모임 추가
     * @param token
     * @param dto
     * @param request
     * @return
     */
    @PostMapping("/add")
    public Map<String, Object> add(@RequestHeader("Authorization") String token, MoimDTO dto, HttpServletRequest request) {
        boolean flag = false;
        map.clear();

        /* 로그인 체크 여부 확인 */
        boolean check = (boolean)userPool.getUser(dto.getMemberid(), request).get("flag");
        if(check) {
            /* 모임 db에 들어갈 컬럼 확인 */
            if(!checkMoim(dto)) {
                map.put("flag", flag);
                return map;
            }

            /* 모임 db 데이터 저장 및 사진 저장 */
            MoimDTO item = moimService.moimAdd(dto);
            if(item != null) {
                sftpUtils.connection();

                boolean upload = sftpUtils.upload(item.getNo(), dto.getPhoto());
                if(upload) {
                    flag = true;
                    map.put("moim", item);
                }
                sftpUtils.disconnection();
            }
        } else {
            map.put("msg", "로그인 필요!");
        }

        map.put("flag", flag);

        return map;
    }

    /**
     * 모임 삭제
     * @param token
     * @param no
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/del")
    public Map<String, Object> del(@RequestHeader("Authorization") String token, @RequestParam int no, @RequestParam String id, HttpServletRequest request) {
        boolean flag = false;
        map.clear();

        /* 로그인 체크 여부 확인 */
        boolean check = (boolean)userPool.getUser(id, request).get("flag");
        if(check) {
            MoimDTO dto = moimService.moimInfo(no);

            if(id.equals(dto.getMemberid())) {
                if (moimService.moimDelete(no)) {
                    sftpUtils.connection();
                    boolean ok = sftpUtils.delete(no, dto);
                    if (ok) {
                        System.out.println("완료!");
                        map.put("flag", true);
                    } else {
                        System.out.println("불완료!");
                        map.put("flag", flag);
                    }
                }
            } else {
                map.put("flag", flag);
                map.put("msg", "본인이 작성한 모임이 아닙니다.");
            }
        }

        return map;
    }

    /**
     * 모임 수정(한 줄 소개, 타이틀, 내용, 사진)
     * @param token
     * @param form
     * @param request
     * @return
     */
    @PutMapping("/edit")
    public Map<String, Object> edit(@RequestHeader("Authorization") String token, MoimFormDTO form, HttpServletRequest request) {
        map.clear();
        System.out.println("token: " + token);
        System.out.println("form: " + form);

        /* 로그인 체크 여부 확인 */
        MoimDTO dto = moimService.moimInfo(form.getNo());
        if(dto != null) {
            String[] oldName = {dto.getPhoto1(), dto.getPhoto2(), dto.getPhoto3()};
            if (form.getInfo() != null)
                dto.setInfo(form.getInfo());
            if (form.getTitle() != null)
                dto.setTitle(form.getTitle());
            if (form.getContent() != null)
                dto.setContent(form.getContent());

            sftpUtils.connection();
            try {
                System.out.println("사진 작업 교체 시작");
                if (form.getPhoto1() != null) {
                    dto.setPhoto1(form.getPhoto1().getOriginalFilename());
                    if (sftpUtils.editImage(dto.getNo(), oldName[0], form.getPhoto1()))
                        System.out.println("1번 사진 변경 완료!");
                }
                if (form.getPhoto2() != null) {
                    dto.setPhoto2(form.getPhoto2().getOriginalFilename());
                    if (sftpUtils.editImage(dto.getNo(), oldName[1], form.getPhoto2()))
                        System.out.println("2번 사진 변경 완료!");
                }
                if (form.getPhoto3() != null) {
                    dto.setPhoto3(form.getPhoto3().getOriginalFilename());
                    if (sftpUtils.editImage(dto.getNo(), oldName[2], form.getPhoto3()))
                        System.out.println("3번 사진 변경 완료!");
                }
                System.out.println("사진 작업 교체 끝");
                sftpUtils.disconnection();
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
                sftpUtils.disconnection();
            }

            dto = moimService.moimEdit(dto);
            map.put("flag", true);
            map.put("dto", dto);
        } else {
            map.put("flag", false);
        }

        return map;
    }

    private boolean checkMoim(MoimDTO dto) {
        if(dto.getMemberid() == null) {
            map.put("msg", "로그인 필요!");
            return false;
        }
        if(dto.getCatno() == 0) {
            map.put("msg", "카테고리 설정!");
            return false;
        }
        if(dto.getInfo() == null) {
            map.put("msg", "한 줄 소개 필요!");
            return false;
        }
        if(dto.getTitle() == null) {
            map.put("msg", "제목 필요!");
            return false;
        }
        if(dto.getContent() == null) {
            map.put("msg", "내용 필요!");
            return false;
        }
        try {
            if (Arrays.stream(dto.getPhoto()).count() != 3) {
                map.put("msg", "사진 3장 필요!");
                return false;
            } else {
                for(MultipartFile mf : dto.getPhoto()) {
                    String[] file = mf.getOriginalFilename().split("\\.");
                    String ext = file[1];
                    if(!(ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg"))) {
                        map.put("msg", "jpg, png 파일만 가능");
                        return false;
                    }
                }
            }
        } catch(NullPointerException e) {
            map.put("msg", "사진 3장 필요!");
            return false;
        }

        return true;
    }

    @GetMapping("/join/{moimno}/{memberid}")
    public Map<String, Object> join(@RequestHeader("Authorization") String token, @PathVariable("moimno") int moimno, @PathVariable("memberid") String memberid) {
        map.clear();
        System.out.println("token: " + token);
        System.out.println("moimno: " + moimno);
        System.out.println("memberId: " + memberid);

        boolean flag = moimService.join(moimno, memberid);
        if(flag) {
            map.put("flag", flag);
        }

        return map;
    }

    @DeleteMapping("/out/{moimno}/{memberid}")
    public Map<String, Object> out(@RequestHeader("Authorization") String token, @PathVariable("moimno") int moimno, @PathVariable("memberid") String memberid) {
        map.clear();

        boolean flag = moimService.out(moimno, memberid);
        if(flag) {
            map.put("flag", true);

            return map;
        }
        map.put("flag", false);

        return map;
    }

    @Autowired
    public void setMoimService(MoimService moimService) {
        this.moimService = moimService;
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

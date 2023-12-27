package com.example.recrewz.somoim;

import com.example.recrewz.common.file.SftpUtils;
import com.example.recrewz.common.info.Info;
import com.example.recrewz.common.user.UserPool;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@RestController
@CrossOrigin("*")
@RequestMapping("/auth/somoim")
public class AuthSomoimController {
    private SomoimService somoimService;
    private SftpUtils sftpUtils;
    private UserPool userPool;
    private Map<String, Object> map = new WeakHashMap<>();

    @PostMapping("/join/{somoimno}/{id}")
    public Map<String, Object> join(@RequestHeader("Authorization") String token, @PathVariable("somoimno") int somoimno, @PathVariable("id") String id) {
        map.clear();
        System.out.println("somoimno: " + somoimno);
        System.out.println("id: " + id);

        boolean check = somoimService.somoimJoin(somoimno, id);
        if(check) {
            map.put("flag", true);
            map.put("msg", "가입 확인!");
        } else {
            map.put("flag", false);
        }

        return map;
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestHeader("Authorization") String token, SomoimDTO form, HttpServletRequest request) {
        map.clear();
        boolean flag = false;

        boolean check = formCheck(form);
        if(check) {
            SomoimDTO dto = somoimService.add(form);
            if(dto != null) {
                String somoimPath = Info.path + "moim" + File.separator + dto.getMoimno() + File.separator + "somoim";
                sftpUtils.connection();
                boolean exists = sftpUtils.exists(somoimPath);
                if(!exists) {
                    boolean mkdir = sftpUtils.mkdir(somoimPath);
                    if(mkdir) {
                        if(sftpUtils.mkdir(somoimPath + File.separator + dto.getNo())) {
                            if(sftpUtils.upload(somoimPath + File.separator + dto.getNo(), form.getMf())) {
                                flag = true;
                            }
                        }
                    }
                } else {
                    if(sftpUtils.mkdir(somoimPath + File.separator + dto.getNo())) {
                        if(sftpUtils.upload(somoimPath + File.separator + dto.getNo(), form.getMf())) {
                            flag = true;
                        }
                    }
                }
            } else {
                map.put("msg", "소모임 추가 실패!");
                map.put("flag", flag);

                return map;
            }
            map.put("msg", "소모임 추가!");
        }
        map.put("flag", flag);

        return map;
    }

    @DeleteMapping("/del/{no}")
    public Map<String, Object> del(@RequestHeader("Authorization") String token, @PathVariable("no") int no, HttpServletRequest request) {
        map.clear();

        SomoimDTO dto = somoimService.getMoimNo(no);
        if(dto != null) {
            boolean check = somoimService.del(no);
            if (check) {
                String somoimPath = Info.path + "moim" + File.separator + dto.getMoimno() + File.separator + "somoim";
                sftpUtils.connection();
                sftpUtils.delete(somoimPath + File.separator + no, dto.getTitle());
                sftpUtils.disconnection();
            }
        } else {
            map.put("flag", false);

            return map;
        }

        map.put("flag", true);

        return map;
    }

    @PutMapping("/edit")
    public Map<String, Object> edit(@RequestHeader("Authorization") String token, SomoimDTO dto, HttpServletRequest request) {
        map.clear();
        boolean flag = false;

        SomoimDTO empty = somoimService.getMoimNo(dto.getNo());
        if(dto.getTitle() != null)
            empty.setTitle(dto.getTitle());
        if(dto.getContent() != null)
            empty.setContent(dto.getContent());
        if(dto.getJdate() != null)
            empty.setJdate(dto.getJdate());
        if(dto.getLoc() != null)
            empty.setLoc(dto.getLoc());
        if(dto.getTotal() != 0)
            empty.setTotal(dto.getTotal());
        if(dto.getMf() != null)
            empty.setPhoto(dto.getMf().getOriginalFilename());

        SomoimDTO item = somoimService.edit(empty);
        if(item == null) {
            map.put("msg", "업데이트 실패!");
            map.put("flag", flag);

            return map;
        } else {
            map.put("dto", item);
        }

        if(dto.getMf() != null) {
            sftpUtils.connection();
            String path = Info.path + "moim" + File.separator + dto.getMoimno() + File.separator + "somoim" + File.separator + dto.getNo();
            boolean check = sftpUtils.exists(path);
            if(check) {
                boolean upload = sftpUtils.upload(path, dto.getMf());
                if(!upload)
                    map.put("msg", "이미지 저장 실패!");
                else
                    flag = true;
            } else {
                check = sftpUtils.mkdir(path);
                if(check) {
                    boolean upload = sftpUtils.upload(path, dto.getMf());
                    if(!upload)
                        map.put("msg", "이미지 저장 실패!");
                    else
                        flag = true;
                } else {
                    map.put("msg", "디렉토리 생성 실패!");
                }
            }
        }

        map.put("flag", flag);

        return map;
    }

    public boolean formCheck(SomoimDTO form) {
        if(form.getTitle() == null) {
            map.put("msg", "제목 입력!");
            return false;
        }

        if(form.getContent() == null) {
            map.put("msg", "내용 입력!");
            return false;
        }

        if(form.getJdate() == null) {
            map.put("msg", "모임 날짜 입력!");
            return false;
        }

        if(form.getLoc() == null) {
            map.put("msg", "모임 장소 입력!");
            return false;
        }

        if(form.getMf() == null) {
            map.put("msg", "대표 사진 추가!");
            return false;
        }

        return true;
    }

    @DeleteMapping("/out/{somoimno}/{id}")
    public Map<String, Object> out(@RequestHeader("Authorization") String token, @PathVariable("somoimno") int somoimno, @PathVariable("id") String id) {
        map.clear();

        boolean flag = somoimService.somoimOut(somoimno, id);
        if(flag)
            map.put("flag", flag);
        else
            map.put("flag", flag);

        return map;
    }

    @Autowired
    public void setSomoimService(SomoimService somoimService) {
        this.somoimService = somoimService;
    }

    @Autowired
    public void setUserPool(UserPool userPool) {
        this.userPool = userPool;
    }

    @Autowired
    public void setSftpUtils(SftpUtils sftpUtils) {
        this.sftpUtils = sftpUtils;
    }
}

package com.example.recrewz.review;

import com.example.recrewz.common.file.SftpUtils;
import com.example.recrewz.common.info.Info;
import com.example.recrewz.common.user.UserPool;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@RestController
@CrossOrigin("*")
@RequestMapping("/auth/review")
public class AuthReviewController {
    private ReviewService reviewService;
    private SftpUtils sftpUtils;
    private Map<String, Object> map = new WeakHashMap<>();
    @PostMapping("/add")
    public Map<String, Object> add(@RequestHeader("Authorization") String token, ReviewDTO form, HttpServletRequest request) {
        map.clear();
        boolean flag = false;

        if(form.getMf1() != null)
            form.setPhoto1(form.getMf1().getOriginalFilename());
        if(form.getMf2() != null)
            form.setPhoto2(form.getMf2().getOriginalFilename());
        if(form.getMf3() != null)
            form.setPhoto3(form.getMf3().getOriginalFilename());

        ReviewInfo review = reviewService.add(form);
        if(review != null) {
            sftpUtils.connection();

            String path = Info.path + "moim" + File.separator + review.getMoimno() + File.separator + "somoim" + File.separator + form.getSomoimno() + File.separator + "review";
            String reviewNoPath = path + File.separator + review.getNo();
            boolean exists = sftpUtils.exists(path);
            if(!exists) {
                System.out.println("path: " + path);
                boolean reviewMkdir = sftpUtils.mkdir(path);
                if(reviewMkdir) {
                    boolean reviewNoMkdir = sftpUtils.mkdir(reviewNoPath);
                    if(!reviewNoMkdir) {
                        map.put("flag", flag);
                        map.put("msg", "리뷰 사진 담을 폴더 업로드 실패!");

                        return map;
                    }
                } else {
                    map.put("flag", flag);
                    map.put("msg", "리뷰 폴더 만들기 실패!");

                    return map;
                }
            } else {
                boolean reviewNoMkdir = sftpUtils.mkdir(reviewNoPath);
                if(!reviewNoMkdir) {
                    map.put("flag", flag);
                    map.put("msg", "리뷰 사진 담을 폴더 업로드 실패!");

                    return map;
                }
            }

            boolean upload1 = false, upload2 = false, upload3 = false;
            if(form.getMf1() != null) {
                upload1 = sftpUtils.upload(reviewNoPath, form.getMf1());
                if(!upload1)
                    map.put("msg", "1. 사진 업로드 실패");
            }
            if(form.getMf2() != null) {
                upload2 = sftpUtils.upload(reviewNoPath, form.getMf2());
                if(!upload2)
                    map.put("msg", "2. 사지 업로드 실패!");
            }
            if(form.getMf3() != null) {
                upload3 = sftpUtils.upload(reviewNoPath, form.getMf3());
                if(!upload3)
                    map.put("msg", "2. 사지 업로드 실패!");
            }

            map.put("flag", flag);
            map.put("msg", "리뷰 작성 성공!");
        } else {
            map.put("msg", "리뷰 입력 실패!");
        }

        return map;
    }

    @PutMapping("/edit")
    public Map<String, Object> edit(@RequestHeader("Authorization") String token, ReviewDTO form, HttpServletRequest request) {
        map.clear();
        boolean flag = false;

        ReviewDTO dto = reviewService.get(form.getNo());
        if(dto != null) {
            sftpUtils.connection();

            String path = Info.path + "moim" + File.separator + form.getMoimno() + File.separator + "somoim" + File.separator + form.getSomoimno() + File.separator + "review";
            boolean exists = sftpUtils.exists(path);

            if(exists) {
                if (form.getMf1() != null) {
                    form.setPhoto1(form.getMf1().getOriginalFilename());
                    boolean photo1 = sftpUtils.deleteProfile(path + File.separator + form.getNo(), dto.getPhoto1());
                    if(photo1) {
                        dto.setPhoto1(form.getPhoto1());
                        sftpUtils.upload(path + File.separator + form.getNo(), form.getMf1());
                    }
                }

                if (form.getMf2() != null) {
                    form.setPhoto2(form.getMf2().getOriginalFilename());
                    boolean photo2 = sftpUtils.deleteProfile(path + File.separator + form.getNo(), dto.getPhoto2());
                    if(photo2) {
                        dto.setPhoto2(form.getPhoto2());
                        sftpUtils.upload(path + File.separator + form.getNo(), form.getMf2());
                    }
                }

                if (form.getMf3() != null) {
                    form.setPhoto3(form.getMf3().getOriginalFilename());
                    boolean photo3 = sftpUtils.deleteProfile(path + File.separator + form.getNo(), dto.getPhoto3());
                    if(photo3) {
                        dto.setPhoto3(form.getPhoto3());
                        sftpUtils.upload(path + File.separator + form.getNo(), form.getMf3());
                    }
                }
            }

            if(form.getTitle() != null)
                dto.setTitle(form.getTitle());
            if(form.getContent() != null)
                dto.setContent(form.getContent());

            ReviewDTO reviewDTO = reviewService.edit(dto);

            map.put("dto", reviewDTO);
            flag = true;
        }

        map.put("flag", flag);

        sftpUtils.disconnection();

        return map;
    }

    @DeleteMapping("/del")
    public Map<String, Object> del(@RequestHeader("Authorization") String token, int no) {
        map.clear();

        ReviewInfo ri = reviewService.info(no);
        boolean flag =reviewService.del(no);
        if(flag) {
            sftpUtils.connection();

            String path = Info.path + "moim" + File.separator + ri.getMoimno() + File.separator
                    + "somoim" + File.separator + ri.getSomoimno() + File.separator
                    + "review";
            if(sftpUtils.delDir(path, String.valueOf(no))) {
                map.put("msg", "모임이 삭제 완료 되었습니다.");
            }
        }

        return map;
    }

    @Autowired
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Autowired
    public void setSftpUtils(SftpUtils sftpUtils) {
        this.sftpUtils = sftpUtils;
    }
}

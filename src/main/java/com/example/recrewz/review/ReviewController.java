package com.example.recrewz.review;

import com.example.recrewz.common.file.SftpUtils;
import com.example.recrewz.common.info.Info;
import com.example.recrewz.common.user.UserPool;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/review")
public class ReviewController {
    private ReviewService reviewService;
    private SftpUtils sftpUtils;
    private Map<String, Object> map = new WeakHashMap<>();

    @GetMapping("/list")
    public synchronized Map<String, Object> list(@RequestParam("no") int no) {
        map.clear();
        System.out.println("no: " + no);

        List<ReviewInfo> list = reviewService.list(no);
        if(!list.isEmpty()) {
            map.put("list", list);
        }

        return map;
    }

    @GetMapping("/img/{moimno}/{somoimno}/{no}/{name}")
    public synchronized ResponseEntity<byte[]> img(@PathVariable("moimno") int moimno, @PathVariable("somoimno") int somoimno, @PathVariable("no") int no, @PathVariable("name") String name) {
        map.clear();
        sftpUtils.connection();

        String path = Info.path + "moim" + File.separator + moimno + File.separator + "somoim" + File.separator + somoimno + File.separator + "review" + File.separator + no;
        System.out.println("path: " + path);
        byte[] bytes = sftpUtils.downloadImg(path, name);

        String[] ext = name.split("\\.");

        HttpHeaders header = new HttpHeaders();

        header.add("Content-Type", "image/" + ext[1]);

        sftpUtils.disconnection();

        return new ResponseEntity<>(bytes, header, HttpStatus.OK);
    }

    @PostMapping("/mylist")
    public Map<String, Object> mylist(@RequestParam("moimno") int moimno, @RequestParam("memberid") String memberid) {
        map.clear();
        System.out.println("moimno: " + moimno);
        System.out.println("memberid: " + memberid);

        List<ReviewInfo> list = reviewService.myList(moimno, memberid);
        if(list.isEmpty())
            map.put("list", null);
        else
            map.put("list", list);

        return map;
    }

    @PostMapping("/add")
    public Map<String, Object> add(ReviewDTO form, HttpServletRequest request) {
        map.clear();
        System.out.println(form);
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

    @Autowired
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Autowired
    public void setSftpUtils(SftpUtils sftpUtils) {
        this.sftpUtils = sftpUtils;
    }
}

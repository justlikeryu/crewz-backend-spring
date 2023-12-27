package com.example.recrewz.somoim;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/somoim")
public class SomoimController {
    private SomoimService somoimService;
    private SftpUtils sftpUtils;
    private UserPool userPool;
    private Map<String, Object> map = new WeakHashMap<>();

    @GetMapping("/list/{no}")
    public Map<String, Object> list(@PathVariable("no") int no, HttpServletRequest request) {
        map.clear();

        ArrayList<SomoimDTO> list = somoimService.list(no);
        if(!list.isEmpty()) {
            map.put("list", list);
        }

        return map;
    }

    @GetMapping("/img/{moimno}/{no}/{photo}")
    public ResponseEntity<byte[]> img(@PathVariable("moimno") int moimno, @PathVariable("no") int no, @PathVariable("photo") String photo) {
        sftpUtils.connection();

        byte[] in = sftpUtils.download(Info.path + "moim" + File.separator + moimno + File.separator + "somoim" + File.separator + no, photo);

        ResponseEntity<byte[]> result = null;
        HttpHeaders header = new HttpHeaders();

        String[] ext = photo.split("\\.");

        header.add("Content-Type", "image/png");
        result = new ResponseEntity<>(in, header, HttpStatus.OK);

        return result;
    }

    @GetMapping("/check")
    public Map<String, Object> check(@RequestParam("id") String id, @RequestParam("somoimno") int somoimno) {
        map.clear();

        SomoimSub item = somoimService.check(somoimno, id);
        System.out.println("item: " + item);
        if(item != null)
            map.put("item", item);
        else
            map.put("item", null);

        return map;
    }

    @GetMapping("/review/check/{id}")
    public Map<String, Object> reviewCheck(@PathVariable("id") String id) {
        map.clear();

        boolean flag = somoimService.reviewCheck(id);
        if(flag)
            map.put("flag", true);
        else
            map.put("flag", false);

        return map;
    }

    @GetMapping("/mysomoim/{id}")
    public Map<String, Object> mysomoim(@PathVariable("id") String id) {
        map.clear();

        ArrayList<SomoimDTO> list= somoimService.mysomoim(id);
        if(!list.isEmpty()) {
            map.put("flag", true);
            map.put("list", list);
        } else {
            map.put("flag", false);
        }

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

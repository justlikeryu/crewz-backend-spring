package com.example.recrewz.common.file;

import com.example.recrewz.common.info.Info;
import com.jcraft.jsch.SftpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

@RestController
@CrossOrigin("*")
public class PhotoController {
    private SftpUtils sftpUtils;
    private final Map<String, Object> map = new WeakHashMap<>();

    @GetMapping("/api/photo/list/{no}")
    public Map<String, Object> list(@PathVariable("no") int no) throws SftpException {
        map.clear();

        sftpUtils.connection();

        if(!sftpUtils.exists(Info.path + "moim" + File.separator + no + File.separator + "photo"))
            sftpUtils.mkdir(Info.path + "moim" + File.separator + no + File.separator + "photo");

        ArrayList<String> list = sftpUtils.listFiles(Info.path + "moim" + File.separator + no + File.separator + "photo");
        if(!list.isEmpty()) {
            map.put("list", list);
        } else {
            map.put("list", null);
        }

        sftpUtils.disconnection();

        return map;
    }

    @GetMapping("/api/photo/img/{no}/{name}")
    public synchronized ResponseEntity<byte[]> getImg(@PathVariable("no") int no, @PathVariable("name") String name) {
//        map.clear();
        sftpUtils.connection();

        byte[] bytes = sftpUtils.downloadImg(Info.path + "moim" + File.separator + no + File.separator + "photo", name);

        String[] ext = name.split("\\.");

        HttpHeaders header = new HttpHeaders();

        header.add("Content-Type", "image/" + ext[1]);

        sftpUtils.disconnection();

        return new ResponseEntity<>(bytes, header, HttpStatus.OK);
    }

    @PostMapping("/auth/photo/add")
    public Map<String, Object> add(@RequestHeader("Authorization") String token, Photo photo) {
        map.clear();
        System.out.println("photo add");
        System.out.println(photo);

        sftpUtils.connection();
        boolean ok = sftpUtils.upload(Info.path + "moim" + File.separator + photo.getNo() + File.separator + "photo", photo.getMf());
        sftpUtils.disconnection();
        System.out.println("ok:" + ok);
        if(ok) {
            map.put("flag", ok);

            return map;
        }

        map.put("flag", ok);

        return map;
    }

    @PostMapping("/auth/photo/del")
    public Map<String, Object> del(@RequestHeader("Authorization") String token, Photo photo) {
        map.clear();

        sftpUtils.connection();
        boolean ok = sftpUtils.deletePhoto(Info.path + "moim" + File.separator + photo.getNo() + File.separator + "photo", photo.getName());
        if(ok) {
            map.put("flag", true);

            return map;
        }

        map.put("flag", false);

        return map;
    }

    @Autowired
    public void setSftpUtils(SftpUtils sftpUtils) {
        this.sftpUtils = sftpUtils;
    }
}

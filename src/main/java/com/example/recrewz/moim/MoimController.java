package com.example.recrewz.moim;

import com.example.recrewz.common.file.SftpUtils;
import com.example.recrewz.common.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/moim")
public class MoimController {
    private MoimService moimService;
    private SftpUtils sftpUtils;
    private final Map<String, Object> map = new WeakHashMap<>();

    @GetMapping("/category")
    public Map<String, Object> list(@RequestParam("catno") int catno) {
        map.clear();

        ArrayList<MoimDTO> list= moimService.list(catno);
        if(list.isEmpty())
            map.put("list", null);
        else
            map.put("list", list);

        return map;
    }

    /**
     * 모임 상세정보
     * @param no
     * @return
     */
    @GetMapping("/info/{no}")
    public Map<String, Object> info(@PathVariable("no") int no) {
        map.clear();

        MoimDTO dto = moimService.moimInfo(no);
        if(dto != null) {
            map.put("dto", dto);
            map.put("flag", true);

            return map;
        }

        map.put("flag", false);

        return map;
    }

    /**
     * 모임 사진(no - 모임 번호 / num - 사진 순서 번호)
     * @param no
     * @param num
     * @return
     */
    @GetMapping("/img/{no}/{num}")
    public synchronized ResponseEntity<byte[]> getImg(@PathVariable("no") int no, @PathVariable("num") int num) {
        map.clear();
        sftpUtils.connection();

        MoimDTO dto = moimService.moimInfo(no);
        byte[] bytes = null;

        String[] ext = null;
        if(num == 1) {
            bytes = sftpUtils.downloadImg(Info.path + "moim" + File.separator + no, dto.getPhoto1());
            ext = dto.getPhoto1().split("\\.");
        }
        else if(num == 2) {
            bytes = sftpUtils.downloadImg(Info.path + "moim" + File.separator + no, dto.getPhoto2());
            ext = dto.getPhoto2().split("\\.");
        }
        else if(num == 3) {
            bytes = sftpUtils.downloadImg(Info.path + "moim" + File.separator + no, dto.getPhoto3());
            ext = dto.getPhoto3().split("\\.");
        }

        HttpHeaders header = new HttpHeaders();

        header.add("Content-Type", "image/" + ext[1]);

        sftpUtils.disconnection();

        return new ResponseEntity<>(bytes, header, HttpStatus.OK);
    }

    @GetMapping("/get")
    public Map<String, Object> get(@RequestParam("moimno") int moimno, @RequestParam("memberid") String memberid) {
        map.clear();

        MoimSubDTO dto = moimService.getSub(moimno, memberid);
        if(dto != null)
            map.put("dto", dto);
        else
            map.put("dto", null);

        return map;
    }

    @GetMapping("/mymoimlist/{id}")
    public Map<String, Object> list(@PathVariable("id") String id) {
        map.clear();

        ArrayList<MoimDTO> list = moimService.mymoimList(id);
        if(!list.isEmpty()) {
            map.put("list", list);
        } else {
            map.put("list", null);
        }

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
}

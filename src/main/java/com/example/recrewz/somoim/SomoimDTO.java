package com.example.recrewz.somoim;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SomoimDTO {
    private int no;
    private int moimno;
    private String memberid;
    private String title;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date jdate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date mdate;
    private String loc;
    private int total;
    private MultipartFile mf;
    private String photo;

    public SomoimDTO(Somoim somoim) {
        this.no = somoim.getNo();
        this.moimno = somoim.getMoimno();
        this.memberid = somoim.getMemberid();
        this.title = somoim.getTitle();
        this.content = somoim.getContent();
        this.jdate = somoim.getJdate();
        this.mdate = somoim.getMdate();
        this.loc = somoim.getLoc();
        this.total = somoim.getTotal();
        this.photo = somoim.getPhoto();
    }
}

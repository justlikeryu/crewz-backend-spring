package com.example.recrewz.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewDTO {
    private int no;
    private String memberid;
    private int moimno;
    private int somoimno;
    private String title;
    private String content;
    private MultipartFile mf1;
    private MultipartFile mf2;
    private MultipartFile mf3;
    private String photo1;
    private String photo2;
    private String photo3;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date date;
}

package com.example.recrewz.member;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MoimFormDTO {
    private int no;
    private int catno;
    private String memberid;
    private String info;
    private String title;
    private String content;
    private MultipartFile photo1;
    private MultipartFile photo2;
    private MultipartFile photo3;
    private Date mdate;
    private int love;
}

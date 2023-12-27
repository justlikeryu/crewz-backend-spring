package com.example.recrewz.moim;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MoimDTO {
    private int no;
    private int catno;
    private String memberid;
    private String info;
    private String title;
    private String content;
    private String photo1;
    private String photo2;
    private String photo3;
    private MultipartFile[] photo;
    private Date mdate;
    private int love;
}

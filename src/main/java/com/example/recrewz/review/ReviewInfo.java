package com.example.recrewz.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewInfo {
    private int no;
    private String memberid;
    private int moimno;
    private int somoimno;
    private String title;
    private String content;
    private String photo1;
    private String photo2;
    private String photo3;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date mdate;
    private String somoimtitle;
    private int categoryno;
    private String name;
}

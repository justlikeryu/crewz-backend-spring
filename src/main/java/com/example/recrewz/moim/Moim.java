package com.example.recrewz.moim;

import lombok.*;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Moim {
    private int no;
    private int catno;
    private String memberid;
    private String info;
    private String title;
    private String content;
    private String photo1;
    private String photo2;
    private String photo3;
    private Date mdate;
    private int love;
}

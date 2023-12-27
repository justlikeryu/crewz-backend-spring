package com.example.recrewz.moim;

import lombok.*;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MoimSubDTO {
    private int moimno;
    private String memberid;
    private Date idate;
    private int black;
}

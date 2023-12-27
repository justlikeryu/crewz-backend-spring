package com.example.recrewz.moim;

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
public class MoimSub {
    private int moimno;
    private String memberid;
    private Date idate;
    private int black;
}

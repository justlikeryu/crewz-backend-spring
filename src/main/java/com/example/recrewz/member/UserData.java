package com.example.recrewz.member;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserData {
    private String ip;
    private String browser;
    private MemberDTO dto;
    private Date date;
}

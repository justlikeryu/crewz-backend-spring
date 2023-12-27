package com.example.recrewz.common.file;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Photo {
    private int no;
    private MultipartFile mf;
    private String name;
}

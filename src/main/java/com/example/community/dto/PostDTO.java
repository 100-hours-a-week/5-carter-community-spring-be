package com.example.community.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDTO {
    private Long userId;
    private String title;
    private String content;
    private String image;
    private LocalDateTime date;
    private int likes;
    private int views;
}

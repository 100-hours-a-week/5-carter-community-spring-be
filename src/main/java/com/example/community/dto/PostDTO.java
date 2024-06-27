package com.example.community.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PostDTO {
    private Long userId;
    private String title;
    private String content;
    private String image;
    private LocalDateTime date;
    private int likes;
    private int views;
}

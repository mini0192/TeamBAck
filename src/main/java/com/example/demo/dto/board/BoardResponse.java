package com.example.demo.dto.board;

import com.example.demo.entity.Board;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {
    private Long id;
    private String title;
    private String body;
    private Integer view;
    private Integer age;
    private Integer satisfaction;
    private Integer likeCount;

    private String memberNickname;


}

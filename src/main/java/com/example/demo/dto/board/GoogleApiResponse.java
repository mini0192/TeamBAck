package com.example.demo.dto.board;

import lombok.Data;

import java.util.List;

@Data
public class GoogleApiResponse {
    private List<Place> places;
}

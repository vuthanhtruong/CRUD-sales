package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDTO {
    private String productId;
    private double averageRating;
    private long totalReviews;
    private long fiveStars;
    private long fourStars;
    private long threeStars;
    private long twoStars;
    private long oneStar;
}

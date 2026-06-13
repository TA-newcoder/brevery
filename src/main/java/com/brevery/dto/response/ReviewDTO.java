package com.brevery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO implements Serializable {

    private Long reviewId;
    private String userName; // To match what we used: userFullName -> userName
    private String userAvatar;
    private String productName;
    private Integer rating;
    private String comment;
    private String status;
    private String adminReply;
    private LocalDateTime createdAt;
}

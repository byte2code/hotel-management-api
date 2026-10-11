package com.cn.hotelDemo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectionRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}

package com.capstone.alertservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    List<Long> userIds;
    String title;
    String message;
    String alertType;
    String severity;
    String locationId;
}

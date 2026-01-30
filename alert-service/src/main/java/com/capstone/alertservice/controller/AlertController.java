package com.capstone.alertservice.controller;

import com.capstone.alertservice.model.Alert;
import com.capstone.alertservice.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Alert>> getUserAlerts(@PathVariable Long userId) {
        return ResponseEntity.ok(alertService.getUserAlerts(userId));
    }
}
package com.capstone.alertservice.repository;

import com.capstone.alertservice.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);
}
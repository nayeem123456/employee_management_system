package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.AiReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiReportHistoryRepository extends JpaRepository<AiReportHistory, Long> {
    List<AiReportHistory> findTop10ByOrderByCreatedAtDesc();
}

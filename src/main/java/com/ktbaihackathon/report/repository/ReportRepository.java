package com.ktbaihackathon.report.repository;

import com.ktbaihackathon.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByUser_UserIdOrderByCreatedAtDesc(Long userId);

    Optional<Report> findByReportIdAndUser_UserId(Long reportId, Long userId);
}

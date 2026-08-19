package com.ktbaihackathon.report.repository;

import com.ktbaihackathon.report.entity.Report;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByUserIdOrderByCreatedAtDesc(Long userId);
}

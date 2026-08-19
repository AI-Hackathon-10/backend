package com.ktbaihackathon.report.repository;

import com.ktbaihackathon.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}

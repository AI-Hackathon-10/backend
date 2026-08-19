package com.ktbaihackathon.common.exception;

import com.ktbaihackathon.common.response.ApiErrorResponse;
import com.ktbaihackathon.report.exception.InvalidReportRequestException;
import com.ktbaihackathon.report.exception.ReportNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidReportRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidReportRequest(InvalidReportRequestException exception) {
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse("INVALID_REPORT_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidHttpRequest(Exception exception) {
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse("INVALID_REPORT_REQUEST", "요청 값을 확인해주세요."));
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleReportNotFound(ReportNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("REPORT_NOT_FOUND", exception.getMessage()));
    }
}

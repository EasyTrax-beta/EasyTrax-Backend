package com.easytrax.easytraxbackend.global.exception.handler;

import com.easytrax.easytraxbackend.global.code.BaseErrorCode;
import com.easytrax.easytraxbackend.global.code.dto.ApiResponse;
import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    // Bean Validation @Valid 오류
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String msg = Optional.ofNullable(fe.getDefaultMessage()).orElse("");
            errors.merge(fe.getField(), msg, (a, b) -> a + ", " + b);
        }

        ApiResponse<Map<String, String>> body =
                ApiResponse.of(ErrorStatus.VALIDATION_FAILED, errors);

        return ResponseEntity
                .status(ErrorStatus.VALIDATION_FAILED.getHttpStatus())
                .headers(headers)
                .body(body);
    }

    // @RequestParam 누락
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ApiResponse<Void> body =
                ApiResponse.of(ErrorStatus.MISSING_PARAMETER, null);

        return ResponseEntity
                .status(ErrorStatus.MISSING_PARAMETER.getHttpStatus())
                .headers(headers)
                .body(body);
    }

    // 잘못된 HTTP method
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ApiResponse<Void> body =
                ApiResponse.of(ErrorStatus.METHOD_NOT_ALLOWED, null);

        return ResponseEntity
                .status(ErrorStatus.METHOD_NOT_ALLOWED.getHttpStatus())
                .headers(headers)
                .body(body);
    }

    // RequestParam 검증 실패 시
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleConstraintViolation(ConstraintViolationException ex) {
        String code = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessageTemplate)  // "{EMAIL_FORMAT_ERROR}"
                .filter(Objects::nonNull)
                .map(template -> template.replaceAll("[{}]", ""))  // "EMAIL_FORMAT_ERROR"
                .filter(c -> !c.isEmpty())
                .findFirst()
                .orElse(ErrorStatus.VALIDATION_FAILED.name());

        BaseErrorCode ec;
        try {
            ec = ErrorStatus.valueOf(code);
        } catch (IllegalArgumentException e) {
            ec = ErrorStatus.VALIDATION_FAILED;  // 안전한 폴백
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.of(ec, null));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMultipartException(MultipartException e) {
        // 타입 기반 안전한 분기
        if (e instanceof MaxUploadSizeExceededException) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(ApiResponse.of(ErrorStatus.FILE_SIZE_EXCEEDED, null));
        }

        // 기타 Multipart 에러
        return ResponseEntity.badRequest()
                .body(ApiResponse.of(ErrorStatus.FILE_UPLOAD_ERROR, null));
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage(), ex);
        BaseErrorCode ec = ErrorStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(ApiResponse.of(ec, null));
    }
}

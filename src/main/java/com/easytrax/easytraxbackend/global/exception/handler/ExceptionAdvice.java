package com.easytrax.easytraxbackend.global.exception.handler;

import com.easytrax.easytraxbackend.global.code.BaseErrorCode;
import com.easytrax.easytraxbackend.global.code.dto.ApiResponse;
import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
    protected ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        String code = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(ErrorStatus.VALIDATION_FAILED.name());

        BaseErrorCode ec = ErrorStatus.valueOf(code);
        ApiResponse<Void> body = ApiResponse.of(ec, null);

        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(body);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMultipartException(MultipartException e) {
        // 크기 제한 관련 오류인지 확인
        if (e.getMessage() != null && e.getMessage().contains("Maximum upload size exceeded")) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(ApiResponse.onFailure("FILE400", "파일 크기가 제한을 초과했습니다.", null));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure("FILE400", "파일 업로드 중 오류가 발생했습니다.", null));
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleAll(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        BaseErrorCode ec = ErrorStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(ApiResponse.of(ec, null));
    }
}

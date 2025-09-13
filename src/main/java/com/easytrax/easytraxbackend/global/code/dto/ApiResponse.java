package com.easytrax.easytraxbackend.global.code.dto;

import com.easytrax.easytraxbackend.global.code.BaseErrorCode;
import com.easytrax.easytraxbackend.global.code.status.SuccessStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> of(BaseErrorCode rc, T data) {
        return rc.toResponse(data);
    }

    public static <T> ApiResponse<T> onSuccess(T result){
        return new ApiResponse<>(true, SuccessStatus.OK.getCode() , SuccessStatus.OK.getMessage(), result);
    }

    public static <T> ApiResponse<T> onSuccess(SuccessStatus status, T result){
        return new ApiResponse<>(true, status.getCode(), status.getMessage(), result);
    }

    public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
        return new ApiResponse<> (false, code, message, data);
    }
}

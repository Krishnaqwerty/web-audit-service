package com.webaudit.exception;

import com.webaudit.constants.AppConstants;
import com.webaudit.constants.ErrorConstants;
import com.webaudit.dto.response.ErrorResponseDto;
import com.webaudit.dto.response.FieldErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseDto> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.warn("Business Exception [{}] on path {}: {}", ex.getErrorCode(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failure on path {}: {} field errors", request.getRequestURI(), ex.getBindingResult().getFieldErrorCount());
        
        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(FieldErrorDto.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .rejectedValue(fieldError.getRejectedValue())
                    .build());
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorConstants.ERR_BAD_REQUEST, "Validation failed for request payload", request, fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled System Exception on path {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                ErrorConstants.ERR_INTERNAL_SERVER_ERROR, 
                "An unexpected internal error occurred. Please try again later.", 
                request, 
                null
        );
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(
            HttpStatus status, 
            String errorCode, 
            String message, 
            HttpServletRequest request, 
            List<FieldErrorDto> fieldErrors
    ) {
        String requestId = MDC.get(AppConstants.MDC_REQUEST_ID_KEY);

        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(Instant.now())
                .requestId(requestId)
                .status(status.value())
                .error(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(status);
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            responseBuilder.header("Retry-After", "60");
        }

        return responseBuilder.body(errorDto);
    }
}

package com.resumeiq.exception;

import com.resumeiq.dto.response.ErrorResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedFileType(
            UnsupportedFileTypeException ex, HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request);
    }

    @ExceptionHandler(ScannedPdfException.class)
    public ResponseEntity<ErrorResponse> handleScannedPdf(
            ScannedPdfException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(ResumeParsingException.class)
    public ResponseEntity<ErrorResponse> handleResumeParsing(
            ResumeParsingException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorage(
            FileStorageException ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "File storage error. Please try again.", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the 5MB limit.", request);
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiService(
            AiServiceException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    @ExceptionHandler(AiRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleAiRateLimit(
            AiRateLimitException ex, HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleResilientRateLimit(
            RequestNotPermitted ex, HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS,
                "AI service is temporarily rate-limited. Please wait a moment and try again.", request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        ));
    }
}
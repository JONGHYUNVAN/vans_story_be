package blog.vans_story_be.global.exception;

import blog.vans_story_be.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리를 위한 핸들러 클래스
 * 애플리케이션에서 발생하는 모든 예외를 일관된 형식으로 처리합니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 모든 예외를 처리하는 핸들러 메서드
     *
     * @param e 발생한 예외
     * @return ApiResponse 형식의 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Validation failed", ex.getBindingResult().toString());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 
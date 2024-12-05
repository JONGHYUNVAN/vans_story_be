package blog.vans_story_be.global.exception;

import blog.vans_story_be.global.response.ApiResponse;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

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
    /**
     * JSON 파싱/타입 변환 예외를 처리하는 핸들러
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("잘못된 요청 형식입니다. 요청 데이터를 확인해주세요."));
    }

    /**
     * CustomException 처리를 위한 핸들러
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException e) {
        if (e.getMessage().contains("not found")) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
        
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 검증 실패 예외를 처리하는 핸들러
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(errorMessage));
    }
} 
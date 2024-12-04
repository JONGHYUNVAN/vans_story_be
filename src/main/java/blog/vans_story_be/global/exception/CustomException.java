package blog.vans_story_be.global.exception;

/**
 * 사용자 정의 예외 클래스
 * 애플리케이션에서 발생하는 비즈니스 예외를 처리합니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
public class CustomException extends RuntimeException {
    /**
     * CustomException 생성자
     *
     * @param message 예외 메시지
     */
    public CustomException(String message) {
        super(message);
    }
} 
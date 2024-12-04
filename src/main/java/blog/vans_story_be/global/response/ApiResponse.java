package blog.vans_story_be.global.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 응답을 위한 공통 응답 객체
 * 모든 API 응답은 이 클래스의 형식을 따릅니다.
 *
 * @param <T> 응답 데이터의 타입
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
@Getter
@NoArgsConstructor
public class ApiResponse<T> {
    /** API 호출 성공 여부 */
    private boolean success;
    /** 응답 데이터 */
    private T data;
    /** 응답 메시지 (주로 에러 메시지) */
    private String message;

    /**
     * ApiResponse 생성자
     *
     * @param success 성공 여부
     * @param data 응답 데이터
     * @param message 응답 메시지
     */
    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    /**
     * 성공 응답을 생성하는 정적 팩토리 메서드
     *
     * @param data 응답 데이터
     * @param <T> 응답 데이터의 타입
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 에러 응답을 생성하는 정적 팩토리 메서드
     *
     * @param message 에러 메시지
     * @param <T> 응답 데이터의 타입
     * @return 에러 응답 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
} 
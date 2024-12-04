package blog.vans_story_be.global.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 날짜와 시간 처리를 위한 유틸리티 클래스
 * 날짜/시간 포맷팅과 관련된 공통 기능을 제공합니다.
 *
 * @author vans
 * @version 1.0.0
 * @since 2024.12.04
 */
public class DateTimeUtils {

    /** 기본 날짜/시간 포맷터 */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * LocalDateTime을 지정된 형식의 문자열로 변환합니다.
     *
     * @param dateTime 변환할 LocalDateTime 객체
     * @return 포맷팅된 날짜/시간 문자열
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
} 
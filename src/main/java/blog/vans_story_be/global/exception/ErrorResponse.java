package blog.vans_story_be.global.exception;

import blog.vans_story_be.global.exception.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String details;
} 
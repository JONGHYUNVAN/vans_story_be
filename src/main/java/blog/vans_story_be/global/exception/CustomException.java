package blog.vans_story_be.global.exception;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
} 
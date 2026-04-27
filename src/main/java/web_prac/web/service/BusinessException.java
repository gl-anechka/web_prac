package web_prac.web.service;

//показать ошибки пользователю
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

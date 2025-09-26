package yunrry.flik.batch.exception;

// 사용자 정의 예외
public class ApiLimitExceededException extends RuntimeException {
    public ApiLimitExceededException(String message) {
        super(message);
    }
}
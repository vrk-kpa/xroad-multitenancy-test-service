package fi.dvv.xroad.multitenancytestservice.error;

public class ValidationException extends IllegalArgumentException {
    public ValidationException(String errorMessage) {
        super(errorMessage);
    }
}

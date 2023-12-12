package fi.dvv.xroad.multitenancytestclient.error;

public class ValidationException extends IllegalArgumentException {
    public ValidationException(String errorMessage) {
        super(errorMessage);
    }
}

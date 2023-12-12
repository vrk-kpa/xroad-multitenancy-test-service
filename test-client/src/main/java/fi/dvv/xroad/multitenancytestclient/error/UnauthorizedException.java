package fi.dvv.xroad.multitenancytestclient.error;

import java.nio.file.AccessDeniedException;

public class UnauthorizedException extends AccessDeniedException {
    public UnauthorizedException(String errorMessage) {
        super(errorMessage);
    }
}

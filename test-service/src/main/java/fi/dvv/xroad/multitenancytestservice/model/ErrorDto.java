package fi.dvv.xroad.multitenancytestservice.model;

public record ErrorDto(String errorMessage, int httpStatus) { }

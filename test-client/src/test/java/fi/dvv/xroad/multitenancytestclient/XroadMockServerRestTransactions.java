package fi.dvv.xroad.multitenancytestclient;

import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.http.HttpStatus;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class XroadMockServerRestTransactions {

    private final String serviceId;

    public XroadMockServerRestTransactions(String serviceId) {
        this.serviceId = serviceId;
    }

    public HttpRequest getLoginRequest() {
        return request().withMethod("GET").withPath("/r1/" + serviceId + "/authenticate")
                .withHeader("X-Road-Client", "CS/ORG/1111/TestClient")
                .withHeader("X-Road-Represented-Party", "GOV/11111-1")
                .withHeader("Member-Username", "GOV/11111-1")
                .withHeader("Member-Password", "password");
    }

    public HttpResponse getLoginResponse() {
        return response().withStatusCode(200)
                .withHeader("Authorization", "Bearer foo")
                .withHeader("content-type", "application/json")
                .withBody("{\"message\": \"Login success\"}");
    }

    public HttpRequest getRandomRequest() {
        return request().withMethod("GET").withPath("/r1/" + serviceId + "/private/random")
                .withHeader("Authorization", "Bearer foo")
                .withHeader("X-Road-Represented-Party", "GOV/11111-1");
    }

    public HttpResponse getRandomResponse() {
        return response().withStatusCode(200)
                .withHeader("content-type", "application/json")
                .withBody("{\"data\": 42}");
    }

    public HttpRequest getRandomWithInvalidTokenRequest() {
        return request().withMethod("GET").withPath("/r1/" + serviceId + "/private/random")
                .withHeader("Authorization", "Bearer bar")
                .withHeader("X-Road-Represented-Party", "GOV/11111-1");
    }

    public HttpResponse getUnauthorizedResponse() {
        return response().withStatusCode(HttpStatus.UNAUTHORIZED.value())
                .withHeader("content-type", "application/json")
                .withBody("{\"errorMessage\": \"No way Josè\"}");
    }

    public HttpRequest getHelloRequest() {
        return request().withMethod("GET").withPath("/r1/" + serviceId + "/private/hello")
                .withHeader("Authorization", "Bearer foo")
                .withHeader("X-Road-Represented-Party", "GOV/11111-1");
    }

    public HttpResponse getHelloResponse() {
        return response().withStatusCode(200)
                .withHeader("content-type", "application/json")
                .withBody("{\"message\": \"Hello\"}");
    }

    public HttpRequest getHelloWithInvalidTokenRequest() {
        return request().withMethod("GET").withPath("/r1/" + serviceId + "/private/hello")
                .withHeader("Authorization", "Bearer bar")
                .withHeader("X-Road-Represented-Party", "GOV/11111-1");
    }
}

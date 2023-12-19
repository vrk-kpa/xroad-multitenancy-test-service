package fi.dvv.xroad.multitenancytestclient;

import fi.dvv.xroad.multitenancytestclient.soap.request.AuthenticateRequest;
import fi.dvv.xroad.multitenancytestclient.soap.request.GetRandomRequest;
import fi.dvv.xroad.multitenancytestclient.soap.request.HelloServiceRequest;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.http.HttpStatus;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class XroadMockServerSoapTransactions {

    private final String clientId;
    private final String serviceId;

    public XroadMockServerSoapTransactions(String clientId, String serviceId) {
        this.clientId = clientId;
        this.serviceId = serviceId;
    }


    public HttpRequest getAuthenticateRequest() {
        return request().withMethod("POST").withPath("/")
                .withBody(new AuthenticateRequest(
                        clientId,
                        serviceId,
                        "GOV",
                        "11111-1",
                        "password").toString())
                .withHeader("content-type", "text/xml");
    }

    public HttpResponse getAuthenticateResponse() {
        return response().withStatusCode(200)
                .withHeader("content-type", "text/xml")
                .withBody("""
                        <somesoap>
                        <extsec:securityToken tokenType="urn:ietf:params:oauth:token-type:jwt">goodtoken</extsec:securityToken>
                        </somesoap>
                        """);

    }

    public HttpRequest getRandomRequest() {
        return request().withMethod("POST").withPath("/")
                .withBody(new GetRandomRequest(
                        clientId,
                        serviceId,
                        "GOV",
                        "11111-1",
                        "goodtoken").toString())
                .withHeader("content-type", "text/xml");
    }

    public HttpResponse getRandomResponse() {
        return response().withStatusCode(200)
                .withHeader("content-type", "text/xml")
                .withBody("""
                        <somesoap>
                        <ts1:getRandomResponse>
                        <ts1:data>42</ts1:data>
                        </ts1:getRandomResponse>
                        </somesoap>
                        """);
    }

    public HttpRequest getRandomWithInvalidTokenRequest() {
        return request().withMethod("POST").withPath("/")
                .withBody(new GetRandomRequest(
                        clientId,
                        serviceId,
                        "GOV",
                        "11111-1",
                        "badtoken").toString())
                .withHeader("content-type", "text/xml");
    }

    public HttpResponse getUnauthorizedResponse() {
        return response().withStatusCode(200)
                .withHeader("content-type", "text/xml")
                .withBody("""
                        <somesoap>
                        <SOAP-ENV:Fault>
                        <faultcode>SOAP-ENV:Client</faultcode>
                        <faultstring>Access denied</faultstring>
                        </SOAP-ENV:Fault>
                        </somesoap>
                        """);
    }

    public HttpRequest getHelloRequest() {
        return request().withMethod("POST").withPath("/")
                .withBody(new HelloServiceRequest(
                        clientId,
                        serviceId,
                        "GOV",
                        "11111-1",
                        "goodtoken",
                        "baz").toString())
                .withHeader("content-type", "text/xml");
    }

    public HttpResponse getHelloResponse() {
        return response().withStatusCode(200)
                .withHeader("content-type", "text/xml")
                .withBody("""
                        <somesoap>
                        <ts1:helloServiceResponse>
                        <ts1:message>Hello</ts1:message>
                        </ts1:helloServiceResponse>
                        </somesoap>
                        """);
    }

    public HttpRequest getHelloWithInvalidTokenRequest() {
        return request().withMethod("POST").withPath("/")
                .withBody(new HelloServiceRequest(
                        clientId,
                        serviceId,
                        "GOV",
                        "11111-1",
                        "badtoken",
                        "baz").toString())
                .withHeader("content-type", "text/xml");
    }
}

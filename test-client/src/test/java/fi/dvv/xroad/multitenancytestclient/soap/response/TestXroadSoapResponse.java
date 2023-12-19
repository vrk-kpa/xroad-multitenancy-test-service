package fi.dvv.xroad.multitenancytestclient.soap.response;

import org.junit.jupiter.api.Test;
import soap.response.XroadSoapResponse;

import static org.assertj.core.api.Assertions.assertThat;


public class TestXroadSoapResponse {
    @Test
    public void soapResponseCanExtractSecurityToken() {
        XroadSoapResponse response = new XroadSoapResponse("""
                <some><tag>
                <foo>content</foo>
                <extsec:securityToken somearg="foo">myotken</extsec:securityToken>
                </tag></some>
                """);

        assertThat(response.getSecurityToken()).isEqualTo("myotken");
    }

    @Test
    public void soapResponseCanExtractRandomNumber() {
        XroadSoapResponse response = new XroadSoapResponse("""
                <some><tag>
                <foo>content</foo>
                <ts1:getRandomResponse>
                <ts1:data>42</ts1:data>
                </ts1:getRandomResponse>
                </tag></some>
                """);

        assertThat(response.getRandomNumber()).isEqualTo(42);
    }

    @Test
    public void soapResponseCanExtractHelloMessage() {
        XroadSoapResponse response = new XroadSoapResponse("""
                <some><tag>
                <foo>content</foo>
                <ts1:helloServiceResponse>
                <ts1:message>Hello</ts1:message>
                </ts1:helloServiceResponse>
                </tag></some>
                """);

        assertThat(response.getHelloMessage()).isEqualTo("Hello");
    }

    @Test
    public void soapResponseCanExtractFaultString() {
        XroadSoapResponse response = new XroadSoapResponse("""
                <some><tag>
                <foo>content</foo>
                <SOAP-ENV:Fault><faultcode>SOAP-ENV:Client</faultcode><faultstring>Access denied</faultstring></SOAP-ENV:Fault>
                </tag></some>
                """);

        assertThat(response.getFault()).isEqualTo("Access denied");
    }

}

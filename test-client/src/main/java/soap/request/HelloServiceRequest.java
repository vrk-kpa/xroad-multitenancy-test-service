package soap.request;

public class HelloServiceRequest extends XroadSoapRequest {
    private final String name;

    public HelloServiceRequest(String xroadClientId, String xroadServiceId, String representedPartyClass, String representedPartyCode, String jwt, String name) {
        super(xroadClientId, xroadServiceId, representedPartyClass, representedPartyCode);
        this.jwt = jwt;
        this.name = name;
    }

    @Override
    public String toString() {
        return """
<soapenv:Envelope
        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
        xmlns:xro="http://x-road.eu/xsd/xroad.xsd"
        xmlns:iden="http://x-road.eu/xsd/identifiers"
        xmlns:repr="http://x-road.eu/xsd/representation.xsd"
        xmlns:extsec="http://x-road.eu/xsd/security-token.xsd"
>
    <soapenv:Header>
        <!-- X-Road client header -->
        %s
        <!-- X-Road service header -->
        %s
        <!-- X-Road represented party header -->
        %s
        <!-- X-Road security token header -->
        %s
        <xro:id>ID11234</xro:id>
        <xro:userId>test</xro:userId>
        <xro:protocolVersion>4.0</xro:protocolVersion>
    </soapenv:Header>
    <soapenv:Body>
        <prod:helloService xmlns:prod="http://test.x-road.global/producer">
            <prod:name>%s</prod:name>
        </prod:helloService>
    </soapenv:Body>
</soapenv:Envelope>
        """.formatted(
                this.getClientHeader(),
                this.getServiceHeader("helloService"),
                this.getRepresentedPartyHeader(),
                this.getSecurityTokenHeader(jwt),
                this.name

        );
    }
}

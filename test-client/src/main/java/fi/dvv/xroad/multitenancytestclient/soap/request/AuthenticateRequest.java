package fi.dvv.xroad.multitenancytestclient.soap.request;

public class AuthenticateRequest extends XroadSoapRequest {
    private final String password;

    public AuthenticateRequest(String xroadClientId, String xroadServiceId, String representedPartyClass, String representedPartyCode, String password) {
        super(xroadClientId, xroadServiceId, representedPartyClass, representedPartyCode);
        this.password = password;
    }

    @Override
    public String toString() {
        return """
<soapenv:Envelope
        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
        xmlns:xro="http://x-road.eu/xsd/xroad.xsd"
        xmlns:iden="http://x-road.eu/xsd/identifiers"
        xmlns:repr="http://x-road.eu/xsd/representation.xsd"
>
    <soapenv:Header>
        <!-- X-Road client header -->
        %s
        <!-- X-Road service header -->
        %s
        <!-- X-Road represented party header -->
        %s
        <xro:id>ID11234</xro:id>
        <xro:userId>test</xro:userId>
        <xro:protocolVersion>4.0</xro:protocolVersion>
    </soapenv:Header>
    <soapenv:Body>
        <prod:authenticate xmlns:prod="http://test.x-road.global/producer">
            <prod:username>%s:%s</prod:username>
            <prod:password>%s</prod:password>
        </prod:authenticate>
    </soapenv:Body>
</soapenv:Envelope>
        """.formatted(
                this.getClientHeader(),
                this.getServiceHeader("authenticate"),
                this.getRepresentedPartyHeader(),
                this.representedPartyClass,
                this.representedPartyCode,
                password
        );
    }
}

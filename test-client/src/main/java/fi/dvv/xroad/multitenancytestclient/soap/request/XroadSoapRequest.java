package fi.dvv.xroad.multitenancytestclient.soap.request;


public abstract class XroadSoapRequest {
    protected String clientInstance;
    protected String clientMemberClass;
    protected String clientMemberCode;
    protected String clientSubsystemCode;

    protected String serviceInstance;
    protected String serviceMemberClass;
    protected String serviceMemberCode;
    protected String serviceSubsystemCode;
    protected String representedPartyClass;
    protected String representedPartyCode;

    protected String jwt;

    public XroadSoapRequest(String xroadClientId, String xroadServiceId, String representedPartyClass, String representedPartyCode) {


        String[] clientParts = xroadClientId.split("/");
        clientInstance = clientParts[0];
        clientMemberClass = clientParts[1];
        clientMemberCode = clientParts[2];
        clientSubsystemCode = clientParts[3];

        String serviceParts[] = xroadServiceId.split("/");
        serviceInstance = serviceParts[0];
        serviceMemberClass = serviceParts[1];
        serviceMemberCode = serviceParts[2];
        serviceSubsystemCode = serviceParts[3];

        this.representedPartyClass = representedPartyClass;
        this.representedPartyCode = representedPartyCode;

        this.jwt = null;
    }

    public Object getJwt() {
        return jwt;
    }
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    protected String getClientHeader(){
        return """
<xro:client iden:objectType="SUBSYSTEM">
    <iden:xRoadInstance>%s</iden:xRoadInstance>
    <iden:memberClass>%s</iden:memberClass>
    <iden:memberCode>%s</iden:memberCode>
    <iden:subsystemCode>%s</iden:subsystemCode>
</xro:client>       
        """.formatted(clientInstance, clientMemberClass, clientMemberCode, clientSubsystemCode);
    }

    protected String getServiceHeader(String serviceCode){
        return """
<xro:service iden:objectType="SERVICE">
    <iden:xRoadInstance>%s</iden:xRoadInstance>
    <iden:memberClass>%s</iden:memberClass>
    <iden:memberCode>%s</iden:memberCode>
    <iden:subsystemCode>%s</iden:subsystemCode>
    <iden:serviceCode>%s</iden:serviceCode>
    <iden:serviceVersion>v1</iden:serviceVersion>
</xro:service>
        """.formatted(serviceInstance, serviceMemberClass, serviceMemberCode, serviceSubsystemCode, serviceCode);
    }

    protected String getRepresentedPartyHeader(){
        return """
<repr:representedParty>
    <repr:partyClass>%s</repr:partyClass>
    <repr:partyCode>%s</repr:partyCode>
</repr:representedParty>
        """.formatted(representedPartyClass, representedPartyCode);
    }

    @Override
    public abstract String toString();

    protected String getSecurityTokenHeader(String jwt) {
        return """
<extsec:securityToken tokenType="urn:ietf:params:oauth:token-type:jwt">
    %s
</extsec:securityToken>
        """.formatted(jwt);
    }
}

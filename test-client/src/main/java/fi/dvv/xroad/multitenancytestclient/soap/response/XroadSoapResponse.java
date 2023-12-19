package fi.dvv.xroad.multitenancytestclient.soap.response;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class XroadSoapResponse {

    private Document document;

    public XroadSoapResponse(String xml) throws RuntimeException {
        this.document = loadXMLFromString(xml);
    }

    /**
     * Get the random number from the response.
     * @return The random number
     * @throws RuntimeException if the response does not contain a random number.
     */
    public int getRandomNumber() {
        NodeList randomResponseTag = document.getElementsByTagName("ts1:getRandomResponse");
        if(randomResponseTag.getLength() == 0)
            throw new RuntimeException("No getRandomResponse tag found");

        NodeList childNodes = randomResponseTag.item(0).getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i).getNodeName().equals("ts1:data")) {
                return Integer.parseInt(childNodes.item(i).getTextContent());
            }
        }

        throw new RuntimeException("No data found in getRandomResponse tag");

    }

    /**
     * Get the hello message from the response.
     * @return The hello message
     * @throws RuntimeException if the response does not contain a hello message.
     */
    public String getHelloMessage() {
        NodeList helloServiceResponseTag = document.getElementsByTagName("ts1:helloServiceResponse");
        if(helloServiceResponseTag.getLength() == 0)
            throw new RuntimeException("No helloServiceResponse tag found");

        NodeList childNodes = helloServiceResponseTag.item(0).getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            if(childNodes.item(i).getNodeName().equals("ts1:message")) {
                return childNodes.item(i).getTextContent();
            }
        }

        throw new RuntimeException("No message found in getHelloResponse tag");
    }

    /**
     * Get the security token from the response.
     * @return The security token or null if response does not contain a token.
     */
    public String getSecurityToken() {
        NodeList securityTokenTag = document.getElementsByTagName("extsec:securityToken");
        if(securityTokenTag.getLength() == 0)
            return null;

        return securityTokenTag.item(0).getTextContent();
    }

    /**
     * Get the fault code from the response.
     * @return The fault code or null if response does not contain a fault tag.
     * @throws RuntimeException if the response does not contains a fault tag but no faultstring.
     */
    public String getFault() {
        NodeList faultTag = document.getElementsByTagName("SOAP-ENV:Fault");
        if(faultTag.getLength() == 0)
            return null;

        for(int i = 0; i < faultTag.getLength(); i++) {
            NodeList childNodes = faultTag.item(i).getChildNodes();
            for(int j = 0; j < childNodes.getLength(); j++) {
                if(childNodes.item(j).getNodeName().equals("faultstring")) {
                    return childNodes.item(j).getTextContent();
                }
            }
        }

        throw new RuntimeException("No faultstring found in Fault tag");
    }

    private static Document loadXMLFromString(String xmlResponse) throws RuntimeException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlResponse));
            return builder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Failed to parse response.", e);
        }
    }

}

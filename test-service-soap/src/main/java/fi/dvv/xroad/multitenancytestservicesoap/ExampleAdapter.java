/**
 * The MIT License
 * Copyright © 2023 Nordic Institute for Interoperability Solutions (NIIS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.dvv.xroad.multitenancytestservicesoap;

import com.nimbusds.jose.JOSEException;
import org.niis.xrd4j.common.exception.XRd4JException;
import org.niis.xrd4j.common.exception.XRd4JRuntimeException;
import org.niis.xrd4j.common.message.ErrorMessage;
import org.niis.xrd4j.common.message.ServiceRequest;
import org.niis.xrd4j.common.message.ServiceResponse;
import org.niis.xrd4j.common.util.PropertiesUtil;
import org.niis.xrd4j.common.util.SOAPHelper;
import org.niis.xrd4j.server.AbstractAdapterServlet;
import org.niis.xrd4j.server.deserializer.AbstractCustomRequestDeserializer;
import org.niis.xrd4j.server.serializer.AbstractServiceResponseSerializer;
import org.niis.xrd4j.server.serializer.ServiceResponseSerializer;

import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import javax.xml.namespace.QName;
import javax.xml.soap.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class implements two simple X-Road 6 and X-Road 7 compatible services:
 * "getRandom" and "helloService". Service descriptions
 * are defined in "test-service-soap.wsdl" file that's located in resources/ folder.
 * The name of the WSDL file and the namespace is configured in resources/xrd-servlet.properties file.
 */
@Component
public class ExampleAdapter extends AbstractAdapterServlet {

    private Properties props;
    private static final Logger logger = LoggerFactory.getLogger(ExampleAdapter.class);
    private String namespaceSerialize;
    private String namespaceDeserialize;
    private String prefix;

    private final JwtService jwtService;

    public ExampleAdapter(JwtService jwtService) {
        super();
        this.jwtService = jwtService;
    }

    @Override
    public void init() {
        logger.debug("Starting to initialize Enpoint.");
        this.props = PropertiesUtil.getInstance().load("/xrd-servlet.properties");
        this.namespaceSerialize = this.props.getProperty("namespace.serialize");
        this.namespaceDeserialize = this.props.getProperty("namespace.deserialize");
        this.prefix = this.props.getProperty("namespace.prefix.serialize");
        logger.debug("Namespace for incoming ServiceRequests : \"" + this.namespaceDeserialize + "\".");
        logger.debug("Namespace for outgoing ServiceResponses : \"" + this.namespaceSerialize + "\".");
        logger.debug("Namespace prefix for outgoing ServiceResponses : \"" + this.prefix + "\".");
        logger.debug("Endpoint initialized.");
    }

    /**
     * Must return the path of the WSDL file.
     *
     * @return absolute path of the WSDL file
     */
    @Override
    protected String getWSDLPath() {
        String path = this.props.getProperty("wsdl.path");
        logger.debug("WSDL path : \"" + path + "\".");
        return path;
    }

    @Override
    protected ServiceResponse handleRequest(ServiceRequest request) throws SOAPException, XRd4JException {
        ServiceResponseSerializer serializer;

        String partyClass = deserializeRepresentedPartyChild(request, "partyClass");
        String partyCode = deserializeRepresentedPartyChild(request, "partyCode");

        String serviceCode = request.getProducer().getServiceCode();

        if ("authenticate".equals(serviceCode)) {
            try {
                return processAuthenticateRequest(request, partyClass, partyCode);
            } catch (JOSEException e) {
                throw new XRd4JRuntimeException("Failed to generate JWT");
            }
        }

        if (!verifyToken(request, partyClass, partyCode)){
            return processUnauthorizedRequest(request);
        }

        // Process services by service code
        if ("getRandom".equals(serviceCode)) {
            return processGetRandomRequest(request);
        } else if ("helloService".equals(serviceCode)) {
            return processHelloServiceRequest(request, partyClass, partyCode);
        }

        // No service matching the service code in the request was found -
        // and error is returned
        serializer = new ServiceResponseSerializerImpl();
        ServiceResponse<String, String> response = new ServiceResponse();
        ErrorMessage error = new ErrorMessage("SOAP-ENV:Client", "Unknown service code.", null, null);
        response.setErrorMessage(error);
        serializer.serialize(response, request);
        return response;
    }

    private boolean verifyToken(ServiceRequest request, String partyClass, String partyCode) {
        String token = request.getSecurityToken();
        return jwtService.validateJwt(token, partyClass + ":" + partyCode);
    }

    private boolean verifyUserAuth(UserAuth userAuth, String partyClass, String partyCode) {
        return (userAuth != null
                && userAuth.getUsername() != null
                && userAuth.getPassword() != null
                && userAuth.getUsername().equals(partyClass + ":" + partyCode)
                && userAuth.getPassword().equals("password"));
    }

    private ServiceResponse<String, String> processAuthenticateRequest(ServiceRequest request, String partyClass, String partyCode) throws SOAPException, XRd4JException, JOSEException {
        ServiceResponseSerializer serializer = new ServiceResponseSerializerImpl();

        logger.info("Process \"authenticate\" service.");

        AuthenticateRequestDeserializer deserializer = new AuthenticateRequestDeserializer();
        deserializer.deserialize(request, this.namespaceDeserialize);

        ServiceResponse<String, String> response = new ServiceResponse<>(request.getConsumer(), request.getProducer(), request.getId());
        response.getProducer().setNamespaceUrl(this.namespaceSerialize);
        response.getProducer().setNamespacePrefix(this.prefix);

        UserAuth userAuth = (UserAuth) request.getRequestData();

        if (!verifyUserAuth(userAuth, partyClass, partyCode)) {
            // No request data is found - an error message is returned
            logger.warn("Authentication failed.");
            ErrorMessage error = new ErrorMessage("SOAP-ENV:Client", "Invalid parameters for authentication.");
            response.setErrorMessage(error);
            serializer.serialize(response, request);
            return response;
        }

        response.setResponseData("OK");

        serializer.serialize(response, request);

        String token = jwtService.generateJwt(partyClass + ":" + partyCode, new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2));

        // Add the token header
        SOAPHeader header = response.getSoapMessage().getSOAPHeader();
        SOAPHeaderElement tokenElement = header.addHeaderElement(new QName("http://x-road.eu/xsd/security-token.xsd", "securityToken", "extsec"));
        tokenElement.addAttribute(new QName("tokenType"), "urn:ietf:params:oauth:token-type:jwt");
        tokenElement.addTextNode(token);


        // Return the response - AbstractAdapterServlet takes care of the rest
        return response;
    }

    private ServiceResponse<String, String> processUnauthorizedRequest(ServiceRequest request) throws SOAPException, XRd4JException {
        ServiceResponseSerializer serializer = new ServiceResponseSerializerImpl();
        ServiceResponse<String, String> response = new ServiceResponse();
        ErrorMessage error = new ErrorMessage("SOAP-ENV:Client", "Access denied, invalid token.", null, null);
        response.setErrorMessage(error);
        serializer.serialize(response, request);
        return response;
    }

    private ServiceResponse<String, String> processHelloServiceRequest(ServiceRequest request, String partyClass, String partyCode) throws SOAPException, XRd4JException {
        ServiceResponseSerializer serializer;
        logger.info("Process \"helloService\" service.");
        serializer = new HelloServiceResponseSerializer();

        HelloRequestDeserializer deserializer = new HelloRequestDeserializer();
        deserializer.deserialize(request, this.namespaceDeserialize);
        ServiceResponse<String, String> response = new ServiceResponse<>(request.getConsumer(), request.getProducer(), request.getId());
        response.getProducer().setNamespaceUrl(this.namespaceSerialize);
        response.getProducer().setNamespacePrefix(this.prefix);

        logger.debug("Do message prosessing...");
        if (request.getRequestData() != null) {
            response.setResponseData("Hello " + request.getRequestData() + ", representing " + partyClass + ":" + partyCode + "! Greetings from adapter server!");
        } else {
            // No request data is found - an error message is returned
            logger.warn("No \"name\" parameter found. Return a non-techinal error message.");
            ErrorMessage error = new ErrorMessage("422", "422 Unprocessable Entity. Missing \"name\" element.");
            response.setErrorMessage(error);
        }
        logger.debug("Message prosessing done!");
        serializer.serialize(response, request);

        // Return the response - AbstractAdapterServlet takes care of the rest
        return response;
    }

    private ServiceResponse<String, String> processGetRandomRequest(ServiceRequest request) throws XRd4JException {
        ServiceResponseSerializer serializer;
        logger.info("Process \"getRandom\" service.");
        serializer = new ServiceResponseSerializerImpl();
        ServiceResponse<String, String> response = new ServiceResponse<>(request.getConsumer(), request.getProducer(), request.getId());
        response.getProducer().setNamespaceUrl(this.namespaceSerialize);
        response.getProducer().setNamespacePrefix(this.prefix);
        response.setResponseData(Integer.toString((int) new Random().nextInt(101)));
        serializer.serialize(response, request);
        // Return the response - AbstractAdapterServlet takes care of the rest
        return response;
    }

    /**
     * Deserialize values of X-Road RepresentedParty header.
     * This should be properly implemented in the xrd4j library.
     *
     */
    private static String deserializeRepresentedPartyChild(ServiceRequest request, String childName) throws SOAPException {
        Node representedParty = SOAPHelper.getNode(request.getSoapMessage().getSOAPHeader(), "representedParty");

        if (representedParty != null) {
            Node childNode = SOAPHelper.getNode(representedParty, childName);
            if(childNode != null)
                return childNode.getTextContent();
        }
        return null;
    }

    /**
     * This class is responsible for serializing response data of getRandom
     * service responses.
     */
    private class ServiceResponseSerializerImpl extends AbstractServiceResponseSerializer {

        @Override
        /**
         * Serializes the response data.
         *
         * @param response ServiceResponse holding the application specific
         * response object
         * @param soapResponse SOAPMessage's response object where the response
         * element is added
         * @param envelope SOAPMessage's SOAPEnvelope object
         */
        public void serializeResponse(ServiceResponse response, SOAPElement soapResponse, SOAPEnvelope envelope) throws SOAPException {
            // Add "data" element
            SOAPElement data = soapResponse.addChildElement(envelope.createName("data"));
            // Put response data inside the "data" element
            data.addTextNode((String) response.getResponseData());
        }
    }

    /**
     * This class is responsible for serializing response data of helloService
     * service responses.
     */
    private class HelloServiceResponseSerializer extends AbstractServiceResponseSerializer {

        @Override
        /**
         * Serializes the response data.
         *
         * @param response ServiceResponse holding the application specific
         * response object
         * @param soapResponse SOAPMessage's response object where the response
         * element is added
         * @param envelope SOAPMessage's SOAPEnvelope object
         */
        public void serializeResponse(ServiceResponse response, SOAPElement soapResponse, SOAPEnvelope envelope) throws SOAPException {
            // Add "message" element
            SOAPElement data = soapResponse.addChildElement(envelope.createName("message"));
            // Put response data inside the "message" element
            data.addTextNode((String) response.getResponseData());
        }
    }

    /**
     * This class is responsible for deserializing request data of helloService
     * service requests. The type declaration "<String>" defines the type of the
     * request data, which in this case is String.
     */
    private class HelloRequestDeserializer extends AbstractCustomRequestDeserializer<String> {

        /**
         * Deserializes the "request" element.
         *
         * @param requestNode request element
         * @return content of the request element
         */
        @Override
        protected String deserializeRequest(Node requestNode, SOAPMessage message) throws SOAPException {
            if (requestNode == null) {
                logger.warn("\"requestNode\" is null. Null is returned.");
                return null;
            }
            for (int i = 0; i < requestNode.getChildNodes().getLength(); i++) {
                // Request data is inside of "name" element
                if (requestNode.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE
                        && "name".equals(requestNode.getChildNodes().item(i).getLocalName())) {
                    logger.debug("Found \"name\" element.");
                    // "name" element was found - return the text content
                    return requestNode.getChildNodes().item(i).getTextContent();
                        }
            }
            logger.warn("No \"name\" element found. Null is returned.");
            return null;
        }
    }

    private class UserAuth {
        private String username;
        private String password;

        public UserAuth(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    private class AuthenticateRequestDeserializer extends AbstractCustomRequestDeserializer<UserAuth> {

        /**
         * Deserializes the "request" element.
         *
         * @param requestNode request element
         * @return content of the request element
         */
        @Override
        protected UserAuth deserializeRequest(Node requestNode, SOAPMessage message) throws SOAPException {
            if (requestNode == null) {
                logger.warn("\"requestNode\" is null. Null is returned.");
                return null;
            }

            Node usernameNode = SOAPHelper.getNode(requestNode, "username");
            Node passwordNode = SOAPHelper.getNode(requestNode, "password");

            if (usernameNode == null || passwordNode == null) {
                logger.warn("\"usernameNode\" or \"passwordNode\" is null. Null is returned.");
                return null;
            }

            return new UserAuth(usernameNode.getTextContent(), passwordNode.getTextContent());
        }
    }
}

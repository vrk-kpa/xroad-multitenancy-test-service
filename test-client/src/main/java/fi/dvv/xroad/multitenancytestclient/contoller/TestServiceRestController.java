package fi.dvv.xroad.multitenancytestclient.contoller;

import fi.dvv.xroad.multitenancytestclient.auth.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionServiceRest;

import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionServiceSoap;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestServiceRestController {
    final private XroadConnectionServiceRest xroadConnectionServiceRest;
    final private XroadConnectionServiceSoap xroadConnectionServiceSoap;

    public TestServiceRestController(XroadConnectionServiceRest xroadConnectionServiceRest, XroadConnectionServiceSoap xroadConnectionServiceSoap) {
        this.xroadConnectionServiceRest = xroadConnectionServiceRest;
        this.xroadConnectionServiceSoap = xroadConnectionServiceSoap;
    }

    @GetMapping("/random")
    public RandomNumberDto getRandomInt(
            @AuthenticationPrincipal ConsumerServiceUser principal,
            @RequestParam(required = false) String protocol
    ) {
        System.out.println("called /random as principal: " + principal.getUsername());
        if(protocol != null && protocol.equals("soap")) {
            return xroadConnectionServiceSoap.makeGetRandomRequest(principal);
        }

        return xroadConnectionServiceRest.getRandom(principal);
    }

    @GetMapping("/hello")
    public MessageDto getGreeting(
            @AuthenticationPrincipal ConsumerServiceUser principal,
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) String name
    ) {
        System.out.println("called /hello as principal: " + principal.getUsername());
        if(protocol != null && protocol.equals("soap")) {
            return xroadConnectionServiceSoap.makeHelloServiceRequest(principal, name);
        }
        return xroadConnectionServiceRest.getHello(principal, name);
    }
}

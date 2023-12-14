package fi.dvv.xroad.multitenancytestclient.contoller;

import fi.dvv.xroad.multitenancytestclient.model.ConsumerServiceUser;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestServiceRestController {
    final private XroadConnectionService xroadConnectionService;

    public TestServiceRestController(XroadConnectionService xroadConnectionService) {
        this.xroadConnectionService = xroadConnectionService;
    }

    @GetMapping("/random")
    public RandomNumberDto getRandomInt(@AuthenticationPrincipal ConsumerServiceUser principal) {
        System.out.println("called /random as principal: " + principal.getUsername());
        System.out.println("principal member code: " + principal.getXroadMemberCode());
        System.out.println("principal member class: " + principal.getXroadMemberClass());
        System.out.println("principal token: " + principal.getToken());
        return xroadConnectionService.getRandom(principal);
    }

    @GetMapping("/hello")
    public MessageDto getGreeting(
            @AuthenticationPrincipal ConsumerServiceUser principal
    ) {
        System.out.println("called /hello as principal: " + principal.getUsername());
        return xroadConnectionService.getHello(principal);
    }
}

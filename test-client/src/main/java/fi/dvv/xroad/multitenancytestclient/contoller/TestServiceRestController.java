package fi.dvv.xroad.multitenancytestclient.contoller;

import fi.dvv.xroad.multitenancytestclient.error.ValidationException;
import fi.dvv.xroad.multitenancytestclient.model.MessageDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestclient.service.XroadConnectionService;
import org.owasp.esapi.ESAPI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestServiceRestController {
    final private XroadConnectionService xroadConnectionService;

    public TestServiceRestController(XroadConnectionService xroadConnectionService) {
        this.xroadConnectionService = xroadConnectionService;
    }

    @GetMapping("/random")
    public RandomNumberDto getRandomInt() {
        return xroadConnectionService.getRandom();
    }

    @GetMapping("/hello")
    public MessageDto getGreeting(@RequestParam(value = "name", defaultValue = "") String name) {
        return xroadConnectionService.getHello(name);
    }

    private Boolean stringIsEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
    private void validateName(String name) {
        if (name.length() > 256)
            throw new ValidationException("Name is too long. Max length is 256 characters.");
    }
}

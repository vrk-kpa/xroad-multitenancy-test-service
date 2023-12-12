package fi.dvv.xroad.multitenancytestclient.contoller;

import fi.dvv.xroad.multitenancytestclient.error.ValidationException;
import fi.dvv.xroad.multitenancytestclient.model.GreetingDto;
import fi.dvv.xroad.multitenancytestclient.model.RandomNumberDto;
import org.owasp.esapi.ESAPI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class TestServiceRestController {
    final private Random randomGenerator = new Random();
    final private int maxRandom = 101;

    @GetMapping("/random")
    public RandomNumberDto getRandomInt() {
        System.out.println("called /random");
        return new RandomNumberDto(randomGenerator.nextInt(maxRandom));
    }

    @GetMapping("/greeting")
    public GreetingDto getGreeting(@RequestParam(value = "name", defaultValue = "") String name) {
        System.out.println("called /greeting");

        String nameOut = "";

        if(!stringIsEmpty(name)) {
            validateName(name);
            nameOut = " " + ESAPI.encoder().encodeForJSON(name);
        }

        return new GreetingDto("Hello" + nameOut + "! Greetings from adapter server!");
    }

    private Boolean stringIsEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
    private void validateName(String name) {
        if (name.length() > 256)
            throw new ValidationException("Name is too long. Max length is 256 characters.");
    }
}

package fi.dvv.xroad.multitenancytestservice.contoller;

import com.nimbusds.jose.JOSEException;
import fi.dvv.xroad.multitenancytestservice.error.UnauthorizedException;
import fi.dvv.xroad.multitenancytestservice.error.ValidationException;
import fi.dvv.xroad.multitenancytestservice.model.MessageDto;
import fi.dvv.xroad.multitenancytestservice.model.RandomNumberDto;
import fi.dvv.xroad.multitenancytestservice.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@RestController
public class MultitenancyTestServiceRestController {
    final private Random randomGenerator = new Random();
    final private int maxRandom = 101;
    final private JwtService jwtService;

    public MultitenancyTestServiceRestController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/login")
    public ResponseEntity<MessageDto> login(
            @RequestHeader("X-Road-Represented-Party") String representedParty,
            @RequestHeader("Member-Username") String username,
            @RequestHeader("Member-Password") String password
    ) throws UnauthorizedException, ValidationException, JOSEException {
        System.out.println("called /login");

        if (username == null || username.isEmpty()) {
            throw new ValidationException("Username is missing");
        }

        if (password == null || !password.equals("password")) {
            throw new UnauthorizedException("Invalid password");
        }

        if (representedParty == null || representedParty.isEmpty()) {
            throw new ValidationException("Represented party is missing");
        }

        String jwt = jwtService.generateJwt(representedParty, Date.from(Instant.now().plusSeconds(60*60*2 /* == 2 hours */)));

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Authorization", "Bearer " + jwt);
        responseHeaders.set("X-Road-Represented-Party", representedParty);

        return new ResponseEntity<>(new MessageDto("Login success."), responseHeaders, HttpStatus.OK);
    }

    @GetMapping("/jwks")
    public String getJwksPublicKey() throws JOSEException {
        System.out.println("called /jwks");
        return jwtService.getJwksPublicKey();
    }

    @GetMapping("private/random")
    public RandomNumberDto getRandomInt() {
        System.out.println("called /private/random");
        return new RandomNumberDto(randomGenerator.nextInt(maxRandom));
    }

    @GetMapping("private/hello")
    public MessageDto getGreeting(@AuthenticationPrincipal String principal) {
        System.out.println("called /private/hello");
        return new MessageDto("Hello " + principal + "! Greetings from adapter server!");
    }
}

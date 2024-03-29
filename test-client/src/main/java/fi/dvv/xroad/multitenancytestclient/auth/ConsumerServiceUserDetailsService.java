package fi.dvv.xroad.multitenancytestclient.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Registered consumer organisations are identified by the CN-name of their certificate.
 * This class mocks a UserDetailsService that would fetch the user details from a database.
 * The user details must include the consumer organisation's X-Road member class and code.
 */
@Service
public class ConsumerServiceUserDetailsService implements UserDetailsService {

        static private final HashMap<String, ConsumerServiceUser> users = new HashMap<>();
        static {
            users.put("org1.com", new ConsumerServiceUser(
                    "org1.com",
                    "password",
                    "GOV",
                    "11111-1"
            ));

            users.put("org2.com", new ConsumerServiceUser(
                    "org2.com",
                    "password",
                    "COM",
                    "22222-2"
            ));
        }

        @Override
        public UserDetails loadUserByUsername(String username) {
            return users.get(username);
        }
}

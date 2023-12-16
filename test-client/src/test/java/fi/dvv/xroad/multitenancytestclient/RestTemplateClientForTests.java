package fi.dvv.xroad.multitenancytestclient;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Used to create a RestTemplate instance for unit tests with the test certificates configured.
 */
@Configuration
public class RestTemplateClientForTests {

    private KeyStore keyStore(String file, char[] password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        File key = ResourceUtils.getFile(file);
        try (InputStream in = new FileInputStream(key)) {
            keyStore.load(in, password);
        }
        return keyStore;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception {

        char[] password = "changeit".toCharArray();

        // Accept any certificate from the test-client server
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        // Add the certificate from external-consumer-keystore.p12 when making requests to test-client
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore("classpath:external-consumer-keystore.p12", password), password)
                .loadTrustMaterial(null, acceptingTrustStrategy).build();

        HttpClientBuilder clientBuilder = HttpClients.custom();

        ConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new DefaultHostnameVerifier());
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory> create()
                        .register("https", sslsf)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();
        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
        clientBuilder.setConnectionManager(connectionManager);

        HttpClient client = clientBuilder.build();
        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(client))
                .build();
    }
}
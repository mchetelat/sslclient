package ch.rockondigital.sslclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component("RestTemplateFactory")
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {

    private RestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${app.server.api.truststore:#{null}}")
    private String truststore;

    @Value("${app.server.api.truststore.password:#{null}}")
    private String truststorePassword;

    public String getTruststorePassword() {
        return truststorePassword != null && !truststorePassword.equals("") ? truststorePassword : null;
    }

    public Resource getTruststoreResource() {
        return truststore != null && !truststore.equals("") ? applicationContext.getResource(truststore) : null;
    }

    @Override
    public void afterPropertiesSet() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final CloseableHttpClient client;
        if (getTruststoreResource() != null) {
            log.debug("Create client for Avaloq api using truststore and ssl verification");
            client = HttpClientBuilder.create()
                    .setSSLSocketFactory(buildSSLSocketFactory(getTruststoreResource(), getTruststorePassword()))
                    .build();
        } else if (true) {
            log.debug("Create client for Avaloq api without using truststore and without ssl verification");
            client = HttpClients.custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
        } else {
            log.debug("Create client for Avaloq api without using truststore and ssl verification");
            client = HttpClientBuilder.create()
                    .build();
        }

        final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(client);

        restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
    }

    @Override
    public RestTemplate getObject() {
        return restTemplate;
    }

    @Override
    public Class<?> getObjectType() {
        return RestTemplate.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private LayeredConnectionSocketFactory buildSSLSocketFactory(Resource trustStoreFile, String password) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

            try (InputStream is = trustStoreFile.getInputStream()) {
                trustStore.load(is, password != null ? password.toCharArray() : null);
                log.debug("Truststore has been loaded {} password set.", password != null ? "WITH" : "WITHOUT");

                SSLContext sslcontext = SSLContexts.custom()
                        .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                        .build();

                return new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
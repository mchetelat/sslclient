package ch.rockondigital.sslclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootTest
class SslserverApplicationTests {

	@Autowired
	@Qualifier("RestTemplateFactory")
	private RestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void sayHello() throws URISyntaxException {
		System.out.println("sayHello");

		final String baseUrl = "https://localhost:8080/hello";
		URI uri = new URI(baseUrl);

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);

		ResponseEntity<?> result = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);

		System.out.println(result.getStatusCodeValue());

		System.out.println("saidHello");
	}

}

package com.pythogorean_apis.pythogorean_apis.vision;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class VisionClient {

    private final RestClient restClient;
    private final String dictionary;
    private final int cardSize;

    public VisionClient(
            @Value("${pythogorean.vision.base-url}") String baseUrl,
            @Value("${pythogorean.vision.connect-timeout-ms}") long connectTimeoutMs,
            @Value("${pythogorean.vision.read-timeout-ms}") long readTimeoutMs,
            @Value("${pythogorean.aruco.dictionary}") String dictionary,
            @Value("${pythogorean.aruco.card-size}") int cardSize) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

        this.dictionary = dictionary;
        this.cardSize = cardSize;
    }

    public String dictionary() {
        return dictionary;
    }

    public byte[] renderCard(int markerId, String label) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cards/{markerId}.png")
                        .queryParam("label", label)
                        .queryParam("dictionary", dictionary)
                        .queryParam("size", cardSize)
                        .build(markerId))
                .retrieve()
                .body(byte[].class);
    }
}

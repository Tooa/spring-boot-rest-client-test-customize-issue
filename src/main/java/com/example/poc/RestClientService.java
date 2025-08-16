package com.example.poc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RestClientService {

    private final RestClient client;

    @Autowired
    public RestClientService(
            ContentInterceptor contentInterceptor,
            RestClient.Builder restClientBuilder,
            @Value("${my-client.base-url}")
            String baseUrl
    ) {

        this.client = restClientBuilder
                .baseUrl(baseUrl)
                .requestInterceptor(contentInterceptor)
                .build();
    }

    public String retrieveFoo() {
        return client
                .get()
                .uri("/foo")
                .retrieve()
                .body(String.class);
    }
}

package com.example.poc;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class ContentInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        var response = execution.execute(request, body);
        // Consume response body twice. Should work because the test enables buffered content
        System.out.println(response.getBody().readAllBytes());
        System.out.println(response.getBody().readAllBytes());

        return response;
    }
}

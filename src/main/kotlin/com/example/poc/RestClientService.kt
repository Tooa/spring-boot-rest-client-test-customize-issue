package com.example.poc

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class RestClientService(
    contentInterceptor: ContentInterceptor,
    restClientBuilder: RestClient.Builder,
    @param:Value("\${my-client.base-url}") private val baseUrl: String,
) {
    private val client = restClientBuilder
        .baseUrl(baseUrl)
        // Register request interceptor consuming the response body twice
        .requestInterceptor(contentInterceptor)
        .build()

    fun retrieveFoo(): String? {
        return client
            .get()
            .uri("/foo")
            .retrieve()
            .body(String::class.java)
    }
}

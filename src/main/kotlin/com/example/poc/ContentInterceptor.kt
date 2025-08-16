package com.example.poc

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component

@Component
class ContentInterceptor: ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val response = execution.execute(request, body)
        // Read response twice. This should work when buffering is enabled
        println(response.body.readAllBytes().decodeToString())
        println(response.body.readAllBytes().decodeToString())

        return response
    }
}

package com.example.poc

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.MockServerRestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import kotlin.test.asserter


@TestConfiguration
private class BufferingRestClientAdditionalBeanTestConfiguration {

    // Provide additional customizer bean
    @Bean
    @Primary
    fun myMockServerRestClientCustomizer(): MockServerRestClientCustomizer {
        val customizer = MockServerRestClientCustomizer()
        customizer.setBufferContent(true)
        return customizer
    }
}

@RestClientTest(
    value = [RestClientService::class],
    properties = [
        "my-client.base-url=http://localhost",
    ],
)
@Import(
    ContentInterceptor::class,
    BufferingRestClientAdditionalBeanTestConfiguration::class
)
internal class RestClientServiceMultipleCustomizerTest @Autowired constructor(
    private val mockRestServiceServer: MockRestServiceServer,
    // This bean is not the one from BufferingRestClientAdditionalBeanTestConfiguration that has buffering enabled
    // private val mockServerRestClientCustomizer: MockServerRestClientCustomizer,
    private val unitUnderTest: RestClientService,
) {

    @Test
    fun testInterceptedRequest() {
        mockRestServiceServer
            .expect(
                requestTo("http://localhost/foo"),
            )
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("hello world", MediaType.TEXT_PLAIN))

        val actual = unitUnderTest.retrieveFoo()

        asserter.assertEquals(null, "hello world", actual)
    }
}

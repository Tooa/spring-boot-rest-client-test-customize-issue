package com.example.poc

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.MockServerRestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import kotlin.test.asserter


@TestConfiguration
// Non-Working Solution: This does not work to load our bean after MockRestServiceServerAutoConfiguration (see README.md)
//@AutoConfigureAfter(MockRestServiceServerAutoConfiguration::class)
private class BufferingRestClientTestConfiguration {

    // Overrides bean from MockRestServiceServerAutoConfiguration
    @Bean
    fun mockServerRestClientCustomizer(): MockServerRestClientCustomizer {
        val customizer = MockServerRestClientCustomizer()
        customizer.setBufferContent(true)
        return customizer
    }
}

@RestClientTest(
    value = [RestClientService::class],
    properties = [
        "my-client.base-url=http://localhost",
        // Allow mockServerRestClientCustomizer override bean. Fails otherwise.
        "spring.main.allow-bean-definition-overriding=true",
    ],
)
@Import(
    ContentInterceptor::class,
    // Workaround: Load our bean after the auto-configuration (see README.md)
//    MockRestServiceServerAutoConfiguration::class,
    BufferingRestClientTestConfiguration::class,
)
internal class RestClientServiceTest @Autowired constructor(
    private val mockRestServiceServer: MockRestServiceServer,
    // This bean is not the one from BufferingRestClientTestConfiguration that has buffering enabled
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

package com.example.poc.example1;

import com.example.poc.ContentInterceptor;
import com.example.poc.RestClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@RestClientTest(
        value = RestClientService.class,
        properties = {
                "my-client.base-url=http://localhost",
                "spring.main.allow-bean-definition-overriding=true"
        })
@Import({ContentInterceptor.class, BufferingRestClientTestConfiguration.class})
public class RestClientServiceTest {

    private final MockRestServiceServer mockRestServiceServer;
    private final RestClientService unitUnderTest;

    @Autowired
    public RestClientServiceTest(
            MockRestServiceServer mockRestServiceServer,
            RestClientService unitUnderTest
    ) {
        this.mockRestServiceServer = mockRestServiceServer;
        this.unitUnderTest = unitUnderTest;
    }

    @Test
    void testInterceptRequest() {
        mockRestServiceServer
                .expect(
                        requestTo("http://localhost/foo")
                )
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("hello world", MediaType.TEXT_PLAIN));

        // Expect the test to pass, because buffered content is enabled. But it fails
        var actual = unitUnderTest.retrieveFoo();

        assertEquals("hello world", actual);
    }
}

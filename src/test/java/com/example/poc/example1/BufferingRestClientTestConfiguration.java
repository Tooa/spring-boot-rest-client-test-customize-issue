package com.example.poc.example1;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.MockServerRestClientCustomizer;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class BufferingRestClientTestConfiguration {

    // Override existing bean
    @Bean
    public MockServerRestClientCustomizer mockServerRestClientCustomizer() {
        var customizer = new MockServerRestClientCustomizer();
        customizer.setBufferContent(true);
        return customizer;
    }
}

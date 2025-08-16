package com.example.poc.example2;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.MockServerRestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class BufferingRestClientAdditionalBeanTestConfiguration {

    // Provide additional customizer bean
    @Bean
    @Primary
    public MockServerRestClientCustomizer myMockServerRestClientCustomizer() {
        var customizer = new MockServerRestClientCustomizer();
        customizer.setBufferContent(true);
        return customizer;
    }
}

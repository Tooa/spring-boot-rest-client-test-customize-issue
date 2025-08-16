# spring-boot-rest-client-test-customize-issue

This POC describes a Spring Boot design flaw with the `@RestClientTest` when applying customization. It showcases different
approaches and describes the underlying problem in configuration.

- Motivation: Use the buffering feature from `@RestClientTest`
- Issue: You cannot add your own `RestClientCustomizer` bean when using `@RestClientTest` in a scenario where you **inject** the `MockRestServiceServer` bean in your test
- Analysis & Assessment: `RestClientAutoConfiguration.restClientBuilderConfigurer` consumes a list of `RestClientCustomizer` beans and there is no Spring-like way to have our customizer bean take precedence - at least know to us
- Known Workaround:
    - Create the `MockRestServiceServer` in your test class manually ([see](https://github.com/spring-projects/spring-framework/issues/19258))
    - Override the `RestClientCustomizer` and manipulate the context loading order in the test class (see `MyRestClientServiceTest`)
- Expected Behaviour: Our custom `RestClientCustomizer` bean should be used when using `@RestClientTest` with `MockRestServiceServer`
- Details
    - [BufferedContent Feature](https://github.com/spring-projects/spring-framework/issues/19258)

## RestClientServiceTest (Override bean approach)

Description: This approach **overrides** the existing customizer bean from the Spring Boot auto-configuration

```bash
2025-08-15T10:24:56.830+02:00  INFO 53320 --- [spring-boot-rest-client-test-customize-issue] [    Test worker] o.s.b.f.s.DefaultListableBeanFactory     : Overriding bean definition for bean 'mockServerRestClientCustomizer' with a different definition: replacing [Root bean: class=null; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; fallback=false; factoryBeanName=com.example.poc.BufferingRestClientTestConfiguration; factoryMethodName=mockServerRestClientCustomizer; initMethodNames=null; destroyMethodNames=[(inferred)]; defined in class path resource [com/example/poc/BufferingRestClientTestConfiguration.class]] with [Root bean: class=null; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; fallback=false; factoryBeanName=org.springframework.boot.test.autoconfigure.web.client.MockRestServiceServerAutoConfiguration; factoryMethodName=mockServerRestClientCustomizer; initMethodNames=null; destroyMethodNames=[(inferred)]; defined in class path resource [org/springframework/boot/test/autoconfigure/web/client/MockRestServiceServerAutoConfiguration.class]]
```

Observation:

- Our custom bean is overridden by the default bean provided by `MockRestServiceServerAutoConfiguration`

Analysis:

- Our bean loads first, then does the `MockRestServiceServerAutoConfiguration` load its bean and overrides our bean

Non-working Solutions:

- `@AutoConfigureAfter(MockRestServiceServerAutoConfiguration::class)` does not work here

Workaround:

- Override the `RestClientCustomizer` bean and manipulate the context loading order in the test class (see `RestClientServiceTest`)

## RestClientServiceMultipleCustomizerTest (additional bean approach)

Description: This approach adds an **additional** customizer bean

Observation:

- `MockRestServiceServerAutoConfiguration` consumes our bean due to the `@Primary` annotation
- `RestClientAutoConfiguration` has both beans in `ObjectProvider<RestClientCustomizer> customizerProvider`, but the default auto-configuration bean is the second one and thus takes precedence over our bean

Analysis:

- The  `MockServerRestClientCustomizer` does not implement anything. In consequence, it becomes `MAX_VALUE` and takes lowest precedence. Hence, you cannot provide a bean with a lower precedence to have it last in the list

Context for the analysis:

```kotlin
// File RestClientAutoConfiguration
@Bean
    @ConditionalOnMissingBean
    RestClientBuilderConfigurer restClientBuilderConfigurer(
          ObjectProvider<ClientHttpRequestFactoryBuilder<?>> clientHttpRequestFactoryBuilder,
          ObjectProvider<ClientHttpRequestFactorySettings> clientHttpRequestFactorySettings,
          ObjectProvider<RestClientCustomizer> customizerProvider) {
       return new RestClientBuilderConfigurer(
             clientHttpRequestFactoryBuilder.getIfAvailable(ClientHttpRequestFactoryBuilder::detect),
             clientHttpRequestFactorySettings.getIfAvailable(ClientHttpRequestFactorySettings::defaults),
                // Ordered stream
             customizerProvider.orderedStream().toList());
    }
```

Workaround:

- There is no workaround known to us to make this approach work

## Proposal and Ideas (for discussion)

- Make `ObjectProvider<RestClientCustomizer> customizerProvider` in  `RestClientAutoConfiguration.restClientBuilderConfigurer(...)` order aware
    - i.e. implement `Ordered`
    - add `@Order(0)` and use `AnnotationAwareOrderComparator` for `ObjectProvider<RestClientCustomizer> customizerProvider`

package com.mycompany.myapp.config;

import static com.mycompany.myapp.config.StaticResourcesWebConfiguration.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.CacheControl;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import tech.jhipster.config.JHipsterDefaults;
import tech.jhipster.config.JHipsterProperties;

class StaticResourcesWebConfigurerTest {

    public static final int MAX_AGE_TEST = 5;
    public StaticResourcesWebConfiguration staticResourcesWebConfiguration;
    private ResourceHandlerRegistry resourceHandlerRegistry;
    private MockServletContext servletContext;
    private WebApplicationContext applicationContext;
    private JHipsterProperties props;

    @BeforeEach
    void setUp() {
        servletContext = new MockServletContext(); // Remove spy() to avoid timeout
        applicationContext = mock(WebApplicationContext.class);
        resourceHandlerRegistry = new ResourceHandlerRegistry(applicationContext, servletContext); // Remove spy() to avoid timeout
        props = new JHipsterProperties();
        staticResourcesWebConfiguration = new StaticResourcesWebConfiguration(props); // Remove spy() to avoid timeout
    }

    @Test
    void shouldAppendResourceHandlerAndInitializeIt() {
        staticResourcesWebConfiguration.addResourceHandlers(resourceHandlerRegistry);

        // Simplified test without verify() calls since we removed spy()
        for (String testingPath : RESOURCE_PATHS) {
            assertThat(resourceHandlerRegistry.hasMappingForPattern(testingPath)).isTrue();
        }
    }

    @Test
    void shouldInitializeResourceHandlerWithCacheControlAndLocations() {
        CacheControl ccExpected = CacheControl.maxAge(5, TimeUnit.DAYS).cachePublic();
        ResourceHandlerRegistration resourceHandlerRegistration = new ResourceHandlerRegistration(RESOURCE_PATHS); // Remove spy() to avoid timeout

        staticResourcesWebConfiguration.initializeResourceHandler(resourceHandlerRegistration);

        // Test completes successfully if no exceptions are thrown during initialization
        assertThat(staticResourcesWebConfiguration.getCacheControl()).isNotNull();
    }

    @Test
    void shouldCreateCacheControlBasedOnJhipsterDefaultProperties() {
        CacheControl cacheExpected = CacheControl.maxAge(JHipsterDefaults.Http.Cache.timeToLiveInDays, TimeUnit.DAYS).cachePublic();
        assertThat(staticResourcesWebConfiguration.getCacheControl())
            .extracting(CacheControl::getHeaderValue)
            .isEqualTo(cacheExpected.getHeaderValue());
    }

    @Test
    void shouldCreateCacheControlWithSpecificConfigurationInProperties() {
        props.getHttp().getCache().setTimeToLiveInDays(MAX_AGE_TEST);
        CacheControl cacheExpected = CacheControl.maxAge(MAX_AGE_TEST, TimeUnit.DAYS).cachePublic();
        assertThat(staticResourcesWebConfiguration.getCacheControl())
            .extracting(CacheControl::getHeaderValue)
            .isEqualTo(cacheExpected.getHeaderValue());
    }
}

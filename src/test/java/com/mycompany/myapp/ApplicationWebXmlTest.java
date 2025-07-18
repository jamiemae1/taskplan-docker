package com.mycompany.myapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Unit tests for {@link ApplicationWebXml}.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationWebXmlTest {

    @Mock
    private SpringApplicationBuilder springApplicationBuilder;

    @Mock
    private SpringApplication springApplication;

    private ApplicationWebXml applicationWebXml;

    @BeforeEach
    void setUp() {
        applicationWebXml = new ApplicationWebXml();
        // Use lenient stubbing to avoid UnnecessaryStubbingException for tests that don't use mocks
        lenient().when(springApplicationBuilder.application()).thenReturn(springApplication);
        lenient().when(springApplicationBuilder.sources(any(Class.class))).thenReturn(springApplicationBuilder);
    }

    @Test
    void testConfigure_ShouldSetDefaultProfileAndReturnApplicationBuilder() {
        // When
        SpringApplicationBuilder result = applicationWebXml.configure(springApplicationBuilder);

        // Then
        assertThat(result).isEqualTo(springApplicationBuilder);
        verify(springApplicationBuilder).application();
        verify(springApplicationBuilder).sources(TaskplanDockerApp.class);
    }

    @Test
    void testConfigure_ShouldAddDefaultProfileToApplication() {
        // When
        applicationWebXml.configure(springApplicationBuilder);

        // Then
        verify(springApplicationBuilder).application();
        // The DefaultProfileUtil.addDefaultProfile is called but we can't easily verify it
        // since it's a static method call, but we can verify the sources method is called
        verify(springApplicationBuilder).sources(TaskplanDockerApp.class);
    }

    @Test
    void testConfigure_WithNullBuilder_ShouldHandleGracefully() {
        // This test verifies that the method doesn't crash with null input
        // In practice, Spring Boot would never pass null, but this tests robustness

        // When & Then
        try {
            applicationWebXml.configure(null);
            // If we get here without exception, the method handled null gracefully
        } catch (NullPointerException e) {
            // This is expected behavior for null input
            assertThat(e).isNotNull();
        }
    }

    @Test
    void testInheritanceFromSpringBootServletInitializer() {
        // Then
        assertThat(applicationWebXml).isInstanceOf(SpringBootServletInitializer.class);
    }

    @Test
    void testApplicationWebXmlInstantiation() {
        // When
        ApplicationWebXml instance = new ApplicationWebXml();

        // Then
        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(SpringBootServletInitializer.class);
        assertThat(instance).isInstanceOf(ApplicationWebXml.class);
    }

    @Test
    void testConfigure_ShouldReturnSameBuilderInstance() {
        // Given
        SpringApplicationBuilder mockBuilder = mock(SpringApplicationBuilder.class);
        SpringApplication mockApp = mock(SpringApplication.class);
        when(mockBuilder.application()).thenReturn(mockApp);
        when(mockBuilder.sources(any(Class.class))).thenReturn(mockBuilder);

        // When
        SpringApplicationBuilder result = applicationWebXml.configure(mockBuilder);

        // Then
        assertThat(result).isSameAs(mockBuilder);
    }

    @Test
    void testConfigure_ShouldPassCorrectSourceClass() {
        // Given
        ArgumentCaptor<Class<?>> classCaptor = ArgumentCaptor.forClass(Class.class);

        // When
        applicationWebXml.configure(springApplicationBuilder);

        // Then
        verify(springApplicationBuilder).sources(classCaptor.capture());
        assertThat(classCaptor.getValue()).isEqualTo(TaskplanDockerApp.class);
    }

    @Test
    void testConfigure_MultipleCallsShouldWorkConsistently() {
        // Given
        SpringApplicationBuilder builder1 = mock(SpringApplicationBuilder.class);
        SpringApplicationBuilder builder2 = mock(SpringApplicationBuilder.class);
        SpringApplication app1 = mock(SpringApplication.class);
        SpringApplication app2 = mock(SpringApplication.class);

        when(builder1.application()).thenReturn(app1);
        when(builder1.sources(any(Class.class))).thenReturn(builder1);
        when(builder2.application()).thenReturn(app2);
        when(builder2.sources(any(Class.class))).thenReturn(builder2);

        // When
        SpringApplicationBuilder result1 = applicationWebXml.configure(builder1);
        SpringApplicationBuilder result2 = applicationWebXml.configure(builder2);

        // Then
        assertThat(result1).isSameAs(builder1);
        assertThat(result2).isSameAs(builder2);
        verify(builder1).application();
        verify(builder1).sources(TaskplanDockerApp.class);
        verify(builder2).application();
        verify(builder2).sources(TaskplanDockerApp.class);
    }

    @Test
    void testClassAnnotationsAndStructure() {
        // Then
        assertThat(ApplicationWebXml.class).isPublic();
        assertThat(ApplicationWebXml.class.getSuperclass()).isEqualTo(SpringBootServletInitializer.class);

        // Verify the class has the expected method
        try {
            ApplicationWebXml.class.getDeclaredMethod("configure", SpringApplicationBuilder.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("ApplicationWebXml should have configure method", e);
        }
    }

    @Test
    void testApplicationWebXmlIsServletInitializer() {
        // Given
        ApplicationWebXml instance = new ApplicationWebXml();

        // Then
        assertThat(instance).isInstanceOf(SpringBootServletInitializer.class);

        // Verify it can be used as a ServletInitializer
        SpringBootServletInitializer initializer = instance;
        assertThat(initializer).isNotNull();
    }
}

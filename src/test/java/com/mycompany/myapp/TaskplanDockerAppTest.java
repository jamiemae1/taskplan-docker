package com.mycompany.myapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;
import tech.jhipster.config.JHipsterConstants;

@ExtendWith(MockitoExtension.class)
class TaskplanDockerAppTest {

    private TaskplanDockerApp app;
    private Environment mockEnv;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        mockEnv = mock(Environment.class);
        app = new TaskplanDockerApp(mockEnv);

        // Set up log capture
        logger = (Logger) LoggerFactory.getLogger(TaskplanDockerApp.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void testConstructor() {
        Environment env = mock(Environment.class);
        TaskplanDockerApp taskplanDockerApp = new TaskplanDockerApp(env);

        assertThat(taskplanDockerApp).isNotNull();
    }

    @Test
    void testInitApplicationWithValidProfiles() {
        when(mockEnv.getActiveProfiles()).thenReturn(new String[] { "test" });

        assertDoesNotThrow(() -> app.initApplication());

        // Verify no error logs
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.stream().anyMatch(event -> event.getLevel() == Level.ERROR)).isFalse();
    }

    @Test
    void testInitApplicationWithDevAndProdProfiles() {
        when(mockEnv.getActiveProfiles()).thenReturn(
            new String[] { JHipsterConstants.SPRING_PROFILE_DEVELOPMENT, JHipsterConstants.SPRING_PROFILE_PRODUCTION }
        );

        app.initApplication();

        // Verify error log for dev and prod profiles
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(
            logsList
                .stream()
                .anyMatch(
                    event -> event.getLevel() == Level.ERROR && event.getFormattedMessage().contains("both the 'dev' and 'prod' profiles")
                )
        ).isTrue();
    }

    @Test
    void testInitApplicationWithDevAndCloudProfiles() {
        when(mockEnv.getActiveProfiles()).thenReturn(
            new String[] { JHipsterConstants.SPRING_PROFILE_DEVELOPMENT, JHipsterConstants.SPRING_PROFILE_CLOUD }
        );

        app.initApplication();

        // Verify error log for dev and cloud profiles
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(
            logsList
                .stream()
                .anyMatch(
                    event -> event.getLevel() == Level.ERROR && event.getFormattedMessage().contains("both the 'dev' and 'cloud' profiles")
                )
        ).isTrue();
    }

    @Test
    void testInitApplicationWithProdProfileOnly() {
        when(mockEnv.getActiveProfiles()).thenReturn(new String[] { JHipsterConstants.SPRING_PROFILE_PRODUCTION });

        app.initApplication();

        // Verify no error logs
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.stream().anyMatch(event -> event.getLevel() == Level.ERROR)).isFalse();
    }

    @Test
    void testInitApplicationWithDevProfileOnly() {
        when(mockEnv.getActiveProfiles()).thenReturn(new String[] { JHipsterConstants.SPRING_PROFILE_DEVELOPMENT });

        app.initApplication();

        // Verify no error logs
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.stream().anyMatch(event -> event.getLevel() == Level.ERROR)).isFalse();
    }

    @Test
    void testInitApplicationWithCloudProfileOnly() {
        when(mockEnv.getActiveProfiles()).thenReturn(new String[] { JHipsterConstants.SPRING_PROFILE_CLOUD });

        app.initApplication();

        // Verify no error logs
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.stream().anyMatch(event -> event.getLevel() == Level.ERROR)).isFalse();
    }

    @Test
    void testInitApplicationWithEmptyProfiles() {
        when(mockEnv.getActiveProfiles()).thenReturn(new String[] {});

        app.initApplication();

        // Verify no error logs
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.stream().anyMatch(event -> event.getLevel() == Level.ERROR)).isFalse();
    }

    @Test
    void testInitApplicationWithMultipleValidProfiles() {
        when(mockEnv.getActiveProfiles()).thenReturn(new String[] { "test", "h2mem", "custom" });

        app.initApplication();

        // Verify no error logs
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.stream().anyMatch(event -> event.getLevel() == Level.ERROR)).isFalse();
    }

    @Test
    void testInitApplicationWithAllThreeInvalidProfiles() {
        when(mockEnv.getActiveProfiles()).thenReturn(
            new String[] {
                JHipsterConstants.SPRING_PROFILE_DEVELOPMENT,
                JHipsterConstants.SPRING_PROFILE_PRODUCTION,
                JHipsterConstants.SPRING_PROFILE_CLOUD,
            }
        );

        app.initApplication();

        // Verify both error logs are present
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(
            logsList
                .stream()
                .anyMatch(
                    event -> event.getLevel() == Level.ERROR && event.getFormattedMessage().contains("both the 'dev' and 'prod' profiles")
                )
        ).isTrue();
        assertThat(
            logsList
                .stream()
                .anyMatch(
                    event -> event.getLevel() == Level.ERROR && event.getFormattedMessage().contains("both the 'dev' and 'cloud' profiles")
                )
        ).isTrue();
    }

    @Test
    void testMainMethodDoesNotThrowException() {
        // Test that main method can be called without throwing exceptions
        // We can't easily test the full Spring Boot startup in a unit test,
        // but we can verify the method exists and doesn't throw immediately
        assertDoesNotThrow(() -> {
            // This would normally start the application, but we'll just verify the method exists
            // by checking if it's callable without immediate exceptions
            TaskplanDockerApp.class.getDeclaredMethod("main", String[].class);
        });
    }

    @Test
    void testLogApplicationStartupMethodExists() {
        // Test that the logApplicationStartup method exists (it's private, so we just check reflection)
        assertDoesNotThrow(() -> {
            TaskplanDockerApp.class.getDeclaredMethod("logApplicationStartup", Environment.class);
        });
    }

    @Test
    void testApplicationHasSpringBootApplicationAnnotation() {
        assertThat(
            TaskplanDockerApp.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class)
        ).isTrue();
    }

    @Test
    void testApplicationHasEnableConfigurationPropertiesAnnotation() {
        assertThat(
            TaskplanDockerApp.class.isAnnotationPresent(org.springframework.boot.context.properties.EnableConfigurationProperties.class)
        ).isTrue();
    }

    @Test
    void testInitApplicationAnnotation() {
        assertDoesNotThrow(() -> {
            var method = TaskplanDockerApp.class.getDeclaredMethod("initApplication");
            assertThat(method.isAnnotationPresent(jakarta.annotation.PostConstruct.class)).isTrue();
        });
    }

    @Test
    void testEnvironmentFieldAccess() {
        // Test that the environment field is properly set in constructor
        Environment testEnv = mock(Environment.class);
        TaskplanDockerApp testApp = new TaskplanDockerApp(testEnv);

        // Since the field is private, we test indirectly by calling initApplication
        when(testEnv.getActiveProfiles()).thenReturn(new String[] { "test" });
        assertDoesNotThrow(() -> testApp.initApplication());
    }

    @Test
    void testProfileValidationLogic() {
        // Test the exact profile validation logic
        when(mockEnv.getActiveProfiles()).thenReturn(
            new String[] { JHipsterConstants.SPRING_PROFILE_DEVELOPMENT, JHipsterConstants.SPRING_PROFILE_PRODUCTION, "other" }
        );

        app.initApplication();

        List<ILoggingEvent> logsList = listAppender.list;
        long errorCount = logsList.stream().filter(event -> event.getLevel() == Level.ERROR).count();

        // Should have exactly one error for dev+prod combination
        assertThat(errorCount).isEqualTo(1);
    }

    @Test
    void testProfileValidationWithCloudAndDev() {
        when(mockEnv.getActiveProfiles()).thenReturn(
            new String[] { JHipsterConstants.SPRING_PROFILE_DEVELOPMENT, JHipsterConstants.SPRING_PROFILE_CLOUD, "other" }
        );

        app.initApplication();

        List<ILoggingEvent> logsList = listAppender.list;
        long errorCount = logsList.stream().filter(event -> event.getLevel() == Level.ERROR).count();

        // Should have exactly one error for dev+cloud combination
        assertThat(errorCount).isEqualTo(1);
    }
}

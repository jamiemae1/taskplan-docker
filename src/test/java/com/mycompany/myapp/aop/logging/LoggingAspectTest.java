package com.mycompany.myapp.aop.logging;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import tech.jhipster.config.JHipsterConstants;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private Environment env;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private LoggingAspect loggingAspect;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setup() {
        loggingAspect = new LoggingAspect(env);

        // Setup logger
        logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        // Setup mocks
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn(LoggingAspect.class.getName());
        when(signature.getName()).thenReturn("testMethod");
    }

    @Test
    void testLogAroundWithDebugEnabled() throws Throwable {
        // Set debug level
        logger.setLevel(Level.DEBUG);

        // Setup mock return values
        Object[] args = new Object[] { "test" };
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("result");

        // Execute method
        Object result = loggingAspect.logAround(joinPoint);

        // Verify
        verify(joinPoint).proceed();
        verify(signature, atLeast(2)).getName();

        // Verify log messages
        assertThat(listAppender.list)
            .extracting(ILoggingEvent::getMessage)
            .contains("Enter: {}() with argument[s] = {}", "Exit: {}() with result = {}");
    }

    @Test
    void testLogAroundWithIllegalArgumentException() throws Throwable {
        // Set debug level
        logger.setLevel(Level.ERROR);

        // Setup mock to throw exception
        Object[] args = new Object[] { "test" };
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("test exception"));

        // Execute and verify exception
        assertThatThrownBy(() -> loggingAspect.logAround(joinPoint))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("test exception");

        // Verify log messages
        assertThat(listAppender.list).extracting(ILoggingEvent::getMessage).contains("Illegal argument: {} in {}()");
    }

    @Test
    void testLogAfterThrowingInDevProfile() {
        // Setup dev profile
        when(env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))).thenReturn(true);

        // Create exception
        Exception e = new RuntimeException("Test exception");
        e.initCause(new IllegalStateException("Test cause"));

        // Execute method
        loggingAspect.logAfterThrowing(joinPoint, e);

        // Verify log messages
        assertThat(listAppender.list)
            .extracting(ILoggingEvent::getMessage)
            .contains("Exception in {}() with cause = '{}' and exception = '{}'");
    }

    @Test
    void testLogAfterThrowingInProdProfile() {
        // Setup prod profile
        when(env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))).thenReturn(false);

        // Create exception
        Exception e = new RuntimeException("Test exception");
        e.initCause(new IllegalStateException("Test cause"));

        // Execute method
        loggingAspect.logAfterThrowing(joinPoint, e);

        // Verify log messages
        assertThat(listAppender.list).extracting(ILoggingEvent::getMessage).contains("Exception in {}() with cause = {}");
    }

    @Test
    void testLogAfterThrowingWithNullCause() {
        // Create exception without cause
        Exception e = new RuntimeException("Test exception");

        // Execute method
        loggingAspect.logAfterThrowing(joinPoint, e);

        // Verify log messages contain NULL for cause
        assertThat(listAppender.list).extracting(ILoggingEvent::getMessage).contains("Exception in {}() with cause = {}");
    }
}

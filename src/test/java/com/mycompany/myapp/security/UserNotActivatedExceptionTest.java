package com.mycompany.myapp.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;

/**
 * Unit tests for {@link UserNotActivatedException}.
 */
class UserNotActivatedExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Given
        String message = "User not activated";

        // When
        UserNotActivatedException exception = new UserNotActivatedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Given
        String message = "User not activated";
        Throwable cause = new RuntimeException("Root cause");

        // When
        UserNotActivatedException exception = new UserNotActivatedException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void testConstructorWithNullMessage() {
        // When
        UserNotActivatedException exception = new UserNotActivatedException(null);

        // Then
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testConstructorWithNullMessageAndCause() {
        // Given
        Throwable cause = new RuntimeException("Root cause");

        // When
        UserNotActivatedException exception = new UserNotActivatedException(null, cause);

        // Then
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testConstructorWithEmptyMessage() {
        // Given
        String message = "";

        // When
        UserNotActivatedException exception = new UserNotActivatedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testConstructorWithMessageAndNullCause() {
        // Given
        String message = "User not activated";

        // When
        UserNotActivatedException exception = new UserNotActivatedException(message, null);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testSerializationAndDeserialization() throws IOException, ClassNotFoundException {
        // Given
        String message = "User not activated";
        Throwable cause = new RuntimeException("Root cause");
        UserNotActivatedException originalException = new UserNotActivatedException(message, cause);

        // When
        byte[] serialized = serializeException(originalException);
        UserNotActivatedException deserializedException = deserializeException(serialized);

        // Then
        assertThat(deserializedException.getMessage()).isEqualTo(originalException.getMessage());
        assertThat(deserializedException.getCause()).isNotNull();
        assertThat(deserializedException.getCause().getMessage()).isEqualTo(originalException.getCause().getMessage());
        assertThat(deserializedException).isInstanceOf(UserNotActivatedException.class);
        assertThat(deserializedException).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void testSerializationWithNullValues() throws IOException, ClassNotFoundException {
        // Given
        UserNotActivatedException originalException = new UserNotActivatedException(null, null);

        // When
        byte[] serialized = serializeException(originalException);
        UserNotActivatedException deserializedException = deserializeException(serialized);

        // Then
        assertThat(deserializedException.getMessage()).isNull();
        assertThat(deserializedException.getCause()).isNull();
        assertThat(deserializedException).isInstanceOf(UserNotActivatedException.class);
    }

    @Test
    void testInheritanceFromAuthenticationException() {
        // Given
        UserNotActivatedException exception = new UserNotActivatedException("Test message");

        // Then
        assertThat(exception).isInstanceOf(AuthenticationException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testExceptionWithLongMessage() {
        // Given
        String longMessage =
            "This is a very long message that contains multiple sentences and should test the behavior of the exception with longer text content. " +
            "It includes various characters and should demonstrate that the exception can handle substantial message content without any issues.";

        // When
        UserNotActivatedException exception = new UserNotActivatedException(longMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo(longMessage);
        assertThat(exception.getMessage().length()).isGreaterThan(100);
    }

    @Test
    void testExceptionWithSpecialCharacters() {
        // Given
        String messageWithSpecialChars = "User 'test@example.com' with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";

        // When
        UserNotActivatedException exception = new UserNotActivatedException(messageWithSpecialChars);

        // Then
        assertThat(exception.getMessage()).isEqualTo(messageWithSpecialChars);
    }

    @Test
    void testExceptionWithUnicodeCharacters() {
        // Given
        String unicodeMessage = "Usuario no activado: ñáéíóú 中文 العربية";

        // When
        UserNotActivatedException exception = new UserNotActivatedException(unicodeMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo(unicodeMessage);
    }

    @Test
    void testExceptionChaining() {
        // Given
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalStateException intermediateCause = new IllegalStateException("Intermediate cause", rootCause);
        UserNotActivatedException exception = new UserNotActivatedException("User not activated", intermediateCause);

        // Then
        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    void testToString() {
        // Given
        String message = "User not activated";
        UserNotActivatedException exception = new UserNotActivatedException(message);

        // When
        String toString = exception.toString();

        // Then
        assertThat(toString).contains(UserNotActivatedException.class.getName());
        assertThat(toString).contains(message);
    }

    @Test
    void testToStringWithCause() {
        // Given
        String message = "User not activated";
        Throwable cause = new RuntimeException("Root cause");
        UserNotActivatedException exception = new UserNotActivatedException(message, cause);

        // When
        String toString = exception.toString();

        // Then
        assertThat(toString).contains(UserNotActivatedException.class.getName());
        assertThat(toString).contains(message);
    }

    private byte[] serializeException(UserNotActivatedException exception) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(exception);
            return baos.toByteArray();
        }
    }

    private UserNotActivatedException deserializeException(byte[] serialized) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serialized); ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (UserNotActivatedException) ois.readObject();
        }
    }
}

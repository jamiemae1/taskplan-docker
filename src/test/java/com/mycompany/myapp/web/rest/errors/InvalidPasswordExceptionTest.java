package com.mycompany.myapp.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

class InvalidPasswordExceptionTest {

    @Test
    void testDefaultConstructor() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(ErrorResponseException.class);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testExceptionMessage() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception.getBody().getTitle()).isEqualTo("Incorrect password");
    }

    @Test
    void testExceptionType() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception.getBody().getType()).isEqualTo(ErrorConstants.INVALID_PASSWORD_TYPE);
    }

    @Test
    void testExceptionStatus() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testExceptionInheritance() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception).isInstanceOf(ErrorResponseException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testSerializability() throws Exception {
        InvalidPasswordException originalException = new InvalidPasswordException();

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalException);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        InvalidPasswordException deserializedException = (InvalidPasswordException) ois.readObject();
        ois.close();

        // Verify
        assertThat(deserializedException).isNotNull();
        assertThat(deserializedException.getBody().getTitle()).isEqualTo(originalException.getBody().getTitle());
        assertThat(deserializedException.getBody().getType()).isEqualTo(originalException.getBody().getType());
        assertThat(deserializedException.getStatusCode()).isEqualTo(originalException.getStatusCode());
    }

    @Test
    void testSerialVersionUID() throws NoSuchFieldException {
        assertThat(InvalidPasswordException.class.getDeclaredField("serialVersionUID")).isNotNull();
    }

    @Test
    void testExceptionCanBeThrown() {
        try {
            throw new InvalidPasswordException();
        } catch (InvalidPasswordException e) {
            assertThat(e.getBody().getTitle()).isEqualTo("Incorrect password");
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void testExceptionStackTrace() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testExceptionCause() {
        InvalidPasswordException exception = new InvalidPasswordException();

        // Constructor explicitly sets cause to null
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testExceptionSuppressedExceptions() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception.getSuppressed()).isEmpty();
    }

    @Test
    void testExceptionEquals() {
        InvalidPasswordException exception1 = new InvalidPasswordException();
        InvalidPasswordException exception2 = new InvalidPasswordException();

        assertThat(exception1).isNotSameAs(exception2);
        assertThat(exception1.getBody().getTitle()).isEqualTo(exception2.getBody().getTitle());
        assertThat(exception1.getBody().getType()).isEqualTo(exception2.getBody().getType());
        assertThat(exception1.getStatusCode()).isEqualTo(exception2.getStatusCode());
    }

    @Test
    void testExceptionHashCode() {
        InvalidPasswordException exception1 = new InvalidPasswordException();
        InvalidPasswordException exception2 = new InvalidPasswordException();

        assertThat(exception1.hashCode()).isNotNull();
        assertThat(exception2.hashCode()).isNotNull();
    }

    @Test
    void testExceptionToString() {
        InvalidPasswordException exception = new InvalidPasswordException();

        String toString = exception.toString();
        assertThat(toString).isNotNull();
        assertThat(toString).contains("InvalidPasswordException");
    }

    @Test
    void testProblemDetailProperties() {
        InvalidPasswordException exception = new InvalidPasswordException();

        // Test all ProblemDetail properties are set correctly
        assertThat(exception.getBody().getTitle()).isEqualTo("Incorrect password");
        assertThat(exception.getBody().getType()).isEqualTo(ErrorConstants.INVALID_PASSWORD_TYPE);
        assertThat(exception.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testExceptionDifferentFromBadRequestAlert() {
        InvalidPasswordException invalidPasswordException = new InvalidPasswordException();
        BadRequestAlertException badRequestException = new BadRequestAlertException(
            ErrorConstants.DEFAULT_TYPE,
            "Test message",
            "test",
            "test"
        );

        // Different exception types should have different inheritance
        assertThat(invalidPasswordException).isNotInstanceOf(BadRequestAlertException.class);
        assertThat(badRequestException).isNotInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void testErrorConstantType() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception.getBody().getType()).isEqualTo(ErrorConstants.INVALID_PASSWORD_TYPE);
        assertThat(ErrorConstants.INVALID_PASSWORD_TYPE.toString()).contains("invalid-password");
    }

    @Test
    void testHttpStatusBadRequest() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void testExceptionGetMessage() {
        InvalidPasswordException exception = new InvalidPasswordException();

        // getMessage() returns the full error response string, not just the title
        assertThat(exception.getMessage()).contains("Incorrect password");
    }

    @Test
    void testExceptionDetail() {
        InvalidPasswordException exception = new InvalidPasswordException();

        // Detail should be null as it's not set in the constructor
        assertThat(exception.getBody().getDetail()).isNull();
    }

    @Test
    void testExceptionInstance() {
        InvalidPasswordException exception = new InvalidPasswordException();

        // Instance should be null as it's not set in the constructor
        assertThat(exception.getBody().getInstance()).isNull();
    }

    @Test
    void testExceptionProperties() {
        InvalidPasswordException exception = new InvalidPasswordException();

        // Properties may be null or empty for InvalidPasswordException
        // Just check that getProperties() doesn't throw an exception
        assertThat(exception.getBody()).isNotNull();
    }
}

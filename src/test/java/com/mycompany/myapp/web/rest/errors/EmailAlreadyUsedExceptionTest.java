package com.mycompany.myapp.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class EmailAlreadyUsedExceptionTest {

    @Test
    void testDefaultConstructor() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(BadRequestAlertException.class);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testExceptionMessage() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertThat(exception.getBody().getTitle()).isEqualTo("Email is already in use!");
    }

    @Test
    void testExceptionType() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertThat(exception.getBody().getType()).isEqualTo(ErrorConstants.EMAIL_ALREADY_USED_TYPE);
    }

    @Test
    void testExceptionEntityName() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        // Access the entityName through the parent class properties
        assertThat(exception.getEntityName()).isEqualTo("userManagement");
    }

    @Test
    void testExceptionErrorKey() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        // Access the errorKey through the parent class properties
        assertThat(exception.getErrorKey()).isEqualTo("emailexists");
    }

    @Test
    void testExceptionInheritance() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertThat(exception).isInstanceOf(BadRequestAlertException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testSerializability() throws Exception {
        EmailAlreadyUsedException originalException = new EmailAlreadyUsedException();

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalException);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EmailAlreadyUsedException deserializedException = (EmailAlreadyUsedException) ois.readObject();
        ois.close();

        // Verify
        assertThat(deserializedException).isNotNull();
        assertThat(deserializedException.getBody().getTitle()).isEqualTo(originalException.getBody().getTitle());
        assertThat(deserializedException.getBody().getType()).isEqualTo(originalException.getBody().getType());
    }

    @Test
    void testSerialVersionUID() throws NoSuchFieldException {
        assertThat(EmailAlreadyUsedException.class.getDeclaredField("serialVersionUID")).isNotNull();
    }

    @Test
    void testExceptionCanBeThrown() {
        try {
            throw new EmailAlreadyUsedException();
        } catch (EmailAlreadyUsedException e) {
            assertThat(e.getBody().getTitle()).isEqualTo("Email is already in use!");
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void testExceptionStackTrace() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testExceptionCause() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        // Default constructor should not set a cause
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testExceptionSuppressedExceptions() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertThat(exception.getSuppressed()).isEmpty();
    }

    @Test
    void testExceptionTitle() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertThat(exception.getBody().getTitle()).isEqualTo("Email is already in use!");
    }

    @Test
    void testExceptionStatus() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertThat(exception.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void testExceptionEquals() {
        EmailAlreadyUsedException exception1 = new EmailAlreadyUsedException();
        EmailAlreadyUsedException exception2 = new EmailAlreadyUsedException();

        // Two instances should have the same properties but be different objects
        assertThat(exception1).isNotSameAs(exception2);
        assertThat(exception1.getBody().getTitle()).isEqualTo(exception2.getBody().getTitle());
        assertThat(exception1.getBody().getType()).isEqualTo(exception2.getBody().getType());
    }

    @Test
    void testExceptionHashCode() {
        EmailAlreadyUsedException exception1 = new EmailAlreadyUsedException();
        EmailAlreadyUsedException exception2 = new EmailAlreadyUsedException();

        // Hash codes might be different for different instances
        assertThat(exception1.hashCode()).isNotNull();
        assertThat(exception2.hashCode()).isNotNull();
    }

    @Test
    void testExceptionToString() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        String toString = exception.toString();
        assertThat(toString).isNotNull();
        assertThat(toString).contains("EmailAlreadyUsedException");
    }
}

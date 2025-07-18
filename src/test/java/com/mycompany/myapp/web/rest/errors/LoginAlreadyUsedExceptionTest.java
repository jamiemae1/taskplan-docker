package com.mycompany.myapp.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class LoginAlreadyUsedExceptionTest {

    @Test
    void testDefaultConstructor() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(BadRequestAlertException.class);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testExceptionMessage() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getBody().getTitle()).isEqualTo("Login name already used!");
    }

    @Test
    void testExceptionType() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getBody().getType()).isEqualTo(ErrorConstants.LOGIN_ALREADY_USED_TYPE);
    }

    @Test
    void testExceptionEntityName() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getEntityName()).isEqualTo("userManagement");
    }

    @Test
    void testExceptionErrorKey() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getErrorKey()).isEqualTo("userexists");
    }

    @Test
    void testExceptionInheritance() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception).isInstanceOf(BadRequestAlertException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    void testSerializability() throws Exception {
        LoginAlreadyUsedException originalException = new LoginAlreadyUsedException();

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalException);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LoginAlreadyUsedException deserializedException = (LoginAlreadyUsedException) ois.readObject();
        ois.close();

        // Verify
        assertThat(deserializedException).isNotNull();
        assertThat(deserializedException.getBody().getTitle()).isEqualTo(originalException.getBody().getTitle());
        assertThat(deserializedException.getBody().getType()).isEqualTo(originalException.getBody().getType());
    }

    @Test
    void testSerialVersionUID() throws NoSuchFieldException {
        assertThat(LoginAlreadyUsedException.class.getDeclaredField("serialVersionUID")).isNotNull();
    }

    @Test
    void testExceptionCanBeThrown() {
        try {
            throw new LoginAlreadyUsedException();
        } catch (LoginAlreadyUsedException e) {
            assertThat(e.getBody().getTitle()).isEqualTo("Login name already used!");
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void testExceptionStackTrace() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace().length).isGreaterThan(0);
    }

    @Test
    void testExceptionCause() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testExceptionSuppressedExceptions() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getSuppressed()).isEmpty();
    }

    @Test
    void testExceptionTitle() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getBody().getTitle()).isEqualTo("Login name already used!");
    }

    @Test
    void testExceptionStatus() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertThat(exception.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void testExceptionEquals() {
        LoginAlreadyUsedException exception1 = new LoginAlreadyUsedException();
        LoginAlreadyUsedException exception2 = new LoginAlreadyUsedException();

        assertThat(exception1).isNotSameAs(exception2);
        assertThat(exception1.getBody().getTitle()).isEqualTo(exception2.getBody().getTitle());
        assertThat(exception1.getBody().getType()).isEqualTo(exception2.getBody().getType());
    }

    @Test
    void testExceptionHashCode() {
        LoginAlreadyUsedException exception1 = new LoginAlreadyUsedException();
        LoginAlreadyUsedException exception2 = new LoginAlreadyUsedException();

        assertThat(exception1.hashCode()).isNotNull();
        assertThat(exception2.hashCode()).isNotNull();
    }

    @Test
    void testExceptionToString() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        String toString = exception.toString();
        assertThat(toString).isNotNull();
        assertThat(toString).contains("LoginAlreadyUsedException");
    }

    @Test
    void testComparisonWithEmailAlreadyUsedException() {
        LoginAlreadyUsedException loginException = new LoginAlreadyUsedException();
        EmailAlreadyUsedException emailException = new EmailAlreadyUsedException();

        // Different exception types should have different messages and types
        assertThat(loginException.getBody().getTitle()).isNotEqualTo(emailException.getBody().getTitle());
        assertThat(loginException.getBody().getType()).isNotEqualTo(emailException.getBody().getType());
        assertThat(loginException.getErrorKey()).isNotEqualTo(emailException.getErrorKey());
    }

    @Test
    void testExceptionProperties() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        // Test all properties are set correctly
        assertThat(exception.getBody().getTitle()).isEqualTo("Login name already used!");
        assertThat(exception.getBody().getType()).isEqualTo(ErrorConstants.LOGIN_ALREADY_USED_TYPE);
        assertThat(exception.getEntityName()).isEqualTo("userManagement");
        assertThat(exception.getErrorKey()).isEqualTo("userexists");
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

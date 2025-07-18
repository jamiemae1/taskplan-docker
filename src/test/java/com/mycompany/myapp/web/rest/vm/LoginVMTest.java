package com.mycompany.myapp.web.rest.vm;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginVMTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testGettersAndSetters() {
        LoginVM loginVM = new LoginVM();

        // Test username
        loginVM.setUsername("testuser");
        assertThat(loginVM.getUsername()).isEqualTo("testuser");

        // Test password
        loginVM.setPassword("testpassword");
        assertThat(loginVM.getPassword()).isEqualTo("testpassword");

        // Test rememberMe
        loginVM.setRememberMe(true);
        assertThat(loginVM.isRememberMe()).isTrue();

        loginVM.setRememberMe(false);
        assertThat(loginVM.isRememberMe()).isFalse();
    }

    @Test
    void testValidLoginVM() {
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("testuser");
        loginVM.setPassword("testpassword");
        loginVM.setRememberMe(true);

        Set<ConstraintViolation<LoginVM>> violations = validator.validate(loginVM);
        assertThat(violations).isEmpty();
    }

    @Test
    void testUsernameValidation() {
        LoginVM loginVM = new LoginVM();
        loginVM.setPassword("testpassword");

        // Test null username
        loginVM.setUsername(null);
        Set<ConstraintViolation<LoginVM>> violations = validator.validate(loginVM);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must not be null");

        // Test empty username
        loginVM.setUsername("");
        violations = validator.validate(loginVM);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("size must be between 1 and 50");

        // Test username too long
        loginVM.setUsername("a".repeat(51));
        violations = validator.validate(loginVM);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("size must be between 1 and 50");

        // Test valid username
        loginVM.setUsername("validuser");
        violations = validator.validate(loginVM);
        assertThat(violations).isEmpty();
    }

    @Test
    void testPasswordValidation() {
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("testuser");

        // Test null password
        loginVM.setPassword(null);
        Set<ConstraintViolation<LoginVM>> violations = validator.validate(loginVM);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must not be null");

        // Test password too short
        loginVM.setPassword("abc");
        violations = validator.validate(loginVM);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("size must be between 4 and 100");

        // Test password too long
        loginVM.setPassword("a".repeat(101));
        violations = validator.validate(loginVM);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("size must be between 4 and 100");

        // Test valid password
        loginVM.setPassword("validpassword");
        violations = validator.validate(loginVM);
        assertThat(violations).isEmpty();
    }

    @Test
    void testToString() {
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("testuser");
        loginVM.setPassword("testpassword");
        loginVM.setRememberMe(true);

        String toString = loginVM.toString();
        assertThat(toString).contains("LoginVM{");
        assertThat(toString).contains("username='testuser'");
        assertThat(toString).contains("rememberMe=true");
        assertThat(toString).doesNotContain("password"); // Password should not be in toString
    }

    @Test
    void testToStringWithFalseRememberMe() {
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("user");
        loginVM.setRememberMe(false);

        String toString = loginVM.toString();
        assertThat(toString).contains("rememberMe=false");
    }
}

package com.mycompany.myapp.web.rest.vm;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ManagedUserVMTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDefaultConstructor() {
        ManagedUserVM managedUserVM = new ManagedUserVM();

        assertThat(managedUserVM.getPassword()).isNull();
        assertThat(managedUserVM.getId()).isNull();
        assertThat(managedUserVM.getLogin()).isNull();
        assertThat(managedUserVM.getFirstName()).isNull();
        assertThat(managedUserVM.getLastName()).isNull();
        assertThat(managedUserVM.getEmail()).isNull();
    }

    @Test
    void testPasswordGetterAndSetter() {
        ManagedUserVM managedUserVM = new ManagedUserVM();

        managedUserVM.setPassword("testpassword");
        assertThat(managedUserVM.getPassword()).isEqualTo("testpassword");

        managedUserVM.setPassword(null);
        assertThat(managedUserVM.getPassword()).isNull();
    }

    @Test
    void testPasswordConstants() {
        assertThat(ManagedUserVM.PASSWORD_MIN_LENGTH).isEqualTo(4);
        assertThat(ManagedUserVM.PASSWORD_MAX_LENGTH).isEqualTo(100);
    }

    @Test
    void testPasswordValidation() {
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("testuser");
        managedUserVM.setEmail("test@example.com");

        // Test valid password
        managedUserVM.setPassword("validpassword");
        Set<ConstraintViolation<ManagedUserVM>> violations = validator.validate(managedUserVM);
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isFalse();

        // Test password too short
        managedUserVM.setPassword("abc");
        violations = validator.validate(managedUserVM);
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isTrue();

        // Test password too long
        managedUserVM.setPassword("a".repeat(101));
        violations = validator.validate(managedUserVM);
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isTrue();

        // Test null password (should be valid since it's not required)
        managedUserVM.setPassword(null);
        violations = validator.validate(managedUserVM);
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isFalse();
    }

    @Test
    void testInheritanceFromAdminUserDTO() {
        ManagedUserVM managedUserVM = new ManagedUserVM();

        // Test inherited properties
        managedUserVM.setId(1L);
        managedUserVM.setLogin("testuser");
        managedUserVM.setFirstName("Test");
        managedUserVM.setLastName("User");
        managedUserVM.setEmail("test@example.com");
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey("en");
        managedUserVM.setImageUrl("http://example.com/image.png");

        assertThat(managedUserVM.getId()).isEqualTo(1L);
        assertThat(managedUserVM.getLogin()).isEqualTo("testuser");
        assertThat(managedUserVM.getFirstName()).isEqualTo("Test");
        assertThat(managedUserVM.getLastName()).isEqualTo("User");
        assertThat(managedUserVM.getEmail()).isEqualTo("test@example.com");
        assertThat(managedUserVM.isActivated()).isTrue();
        assertThat(managedUserVM.getLangKey()).isEqualTo("en");
        assertThat(managedUserVM.getImageUrl()).isEqualTo("http://example.com/image.png");
    }

    @Test
    void testToString() {
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("testuser");
        managedUserVM.setFirstName("Test");
        managedUserVM.setLastName("User");
        managedUserVM.setEmail("test@example.com");
        managedUserVM.setPassword("testpassword");

        String toString = managedUserVM.toString();
        assertThat(toString).contains("ManagedUserVM{");
        assertThat(toString).contains("AdminUserDTO{");
        assertThat(toString).doesNotContain("password"); // Password should not be in toString
    }

    @Test
    void testToStringWithNullValues() {
        ManagedUserVM managedUserVM = new ManagedUserVM();

        String toString = managedUserVM.toString();
        assertThat(toString).contains("ManagedUserVM{");
        assertThat(toString).contains("AdminUserDTO{");
    }

    @Test
    void testAuditingFields() {
        ManagedUserVM managedUserVM = new ManagedUserVM();
        Instant now = Instant.now();

        managedUserVM.setCreatedBy("system");
        managedUserVM.setCreatedDate(now);
        managedUserVM.setLastModifiedBy("admin");
        managedUserVM.setLastModifiedDate(now);

        assertThat(managedUserVM.getCreatedBy()).isEqualTo("system");
        assertThat(managedUserVM.getCreatedDate()).isEqualTo(now);
        assertThat(managedUserVM.getLastModifiedBy()).isEqualTo("admin");
        assertThat(managedUserVM.getLastModifiedDate()).isEqualTo(now);
    }

    @Test
    void testAuthorities() {
        ManagedUserVM managedUserVM = new ManagedUserVM();
        Set<String> authorities = Set.of("ROLE_USER", "ROLE_ADMIN");

        managedUserVM.setAuthorities(authorities);
        assertThat(managedUserVM.getAuthorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void testMinimumPasswordLength() {
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("testuser");
        managedUserVM.setEmail("test@example.com");

        // Test minimum valid password length
        managedUserVM.setPassword("1234");
        Set<ConstraintViolation<ManagedUserVM>> violations = validator.validate(managedUserVM);
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isFalse();
    }

    @Test
    void testMaximumPasswordLength() {
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("testuser");
        managedUserVM.setEmail("test@example.com");

        // Test maximum valid password length
        managedUserVM.setPassword("a".repeat(100));
        Set<ConstraintViolation<ManagedUserVM>> violations = validator.validate(managedUserVM);
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isFalse();
    }
}

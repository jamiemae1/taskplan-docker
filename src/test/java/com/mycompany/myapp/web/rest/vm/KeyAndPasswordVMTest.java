package com.mycompany.myapp.web.rest.vm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KeyAndPasswordVMTest {

    @Test
    void testGettersAndSetters() {
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();

        // Test key
        keyAndPasswordVM.setKey("testkey123");
        assertThat(keyAndPasswordVM.getKey()).isEqualTo("testkey123");

        // Test newPassword
        keyAndPasswordVM.setNewPassword("newpassword123");
        assertThat(keyAndPasswordVM.getNewPassword()).isEqualTo("newpassword123");
    }

    @Test
    void testDefaultConstructor() {
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();

        assertThat(keyAndPasswordVM.getKey()).isNull();
        assertThat(keyAndPasswordVM.getNewPassword()).isNull();
    }

    @Test
    void testNullValues() {
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();

        keyAndPasswordVM.setKey(null);
        keyAndPasswordVM.setNewPassword(null);

        assertThat(keyAndPasswordVM.getKey()).isNull();
        assertThat(keyAndPasswordVM.getNewPassword()).isNull();
    }

    @Test
    void testEmptyValues() {
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();

        keyAndPasswordVM.setKey("");
        keyAndPasswordVM.setNewPassword("");

        assertThat(keyAndPasswordVM.getKey()).isEqualTo("");
        assertThat(keyAndPasswordVM.getNewPassword()).isEqualTo("");
    }

    @Test
    void testLongValues() {
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();

        String longKey = "a".repeat(1000);
        String longPassword = "b".repeat(1000);

        keyAndPasswordVM.setKey(longKey);
        keyAndPasswordVM.setNewPassword(longPassword);

        assertThat(keyAndPasswordVM.getKey()).isEqualTo(longKey);
        assertThat(keyAndPasswordVM.getNewPassword()).isEqualTo(longPassword);
    }

    @Test
    void testSpecialCharacters() {
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();

        String specialKey = "key!@#$%^&*()";
        String specialPassword = "pass!@#$%^&*()";

        keyAndPasswordVM.setKey(specialKey);
        keyAndPasswordVM.setNewPassword(specialPassword);

        assertThat(keyAndPasswordVM.getKey()).isEqualTo(specialKey);
        assertThat(keyAndPasswordVM.getNewPassword()).isEqualTo(specialPassword);
    }
}

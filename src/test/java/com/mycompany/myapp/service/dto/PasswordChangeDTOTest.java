package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;

class PasswordChangeDTOTest {

    @Test
    void testDefaultConstructor() {
        PasswordChangeDTO dto = new PasswordChangeDTO();

        assertThat(dto.getCurrentPassword()).isNull();
        assertThat(dto.getNewPassword()).isNull();
    }

    @Test
    void testParameterizedConstructor() {
        String currentPassword = "currentPass123";
        String newPassword = "newPass456";

        PasswordChangeDTO dto = new PasswordChangeDTO(currentPassword, newPassword);

        assertThat(dto.getCurrentPassword()).isEqualTo(currentPassword);
        assertThat(dto.getNewPassword()).isEqualTo(newPassword);
    }

    @Test
    void testParameterizedConstructorWithNullValues() {
        PasswordChangeDTO dto = new PasswordChangeDTO(null, null);

        assertThat(dto.getCurrentPassword()).isNull();
        assertThat(dto.getNewPassword()).isNull();
    }

    @Test
    void testGettersAndSetters() {
        PasswordChangeDTO dto = new PasswordChangeDTO();

        // Test currentPassword
        dto.setCurrentPassword("currentPassword");
        assertThat(dto.getCurrentPassword()).isEqualTo("currentPassword");

        // Test newPassword
        dto.setNewPassword("newPassword");
        assertThat(dto.getNewPassword()).isEqualTo("newPassword");
    }

    @Test
    void testSettersWithNullValues() {
        PasswordChangeDTO dto = new PasswordChangeDTO("initial", "initial");

        dto.setCurrentPassword(null);
        dto.setNewPassword(null);

        assertThat(dto.getCurrentPassword()).isNull();
        assertThat(dto.getNewPassword()).isNull();
    }

    @Test
    void testSettersWithEmptyValues() {
        PasswordChangeDTO dto = new PasswordChangeDTO();

        dto.setCurrentPassword("");
        dto.setNewPassword("");

        assertThat(dto.getCurrentPassword()).isEqualTo("");
        assertThat(dto.getNewPassword()).isEqualTo("");
    }

    @Test
    void testSettersWithLongValues() {
        PasswordChangeDTO dto = new PasswordChangeDTO();

        String longPassword = "a".repeat(1000);
        dto.setCurrentPassword(longPassword);
        dto.setNewPassword(longPassword);

        assertThat(dto.getCurrentPassword()).isEqualTo(longPassword);
        assertThat(dto.getNewPassword()).isEqualTo(longPassword);
    }

    @Test
    void testSettersWithSpecialCharacters() {
        PasswordChangeDTO dto = new PasswordChangeDTO();

        String specialPassword = "pass!@#$%^&*()_+-=[]{}|;':\",./<>?";
        dto.setCurrentPassword(specialPassword);
        dto.setNewPassword(specialPassword);

        assertThat(dto.getCurrentPassword()).isEqualTo(specialPassword);
        assertThat(dto.getNewPassword()).isEqualTo(specialPassword);
    }

    @Test
    void testSerialization() throws Exception {
        PasswordChangeDTO originalDto = new PasswordChangeDTO("currentPass", "newPass");

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalDto);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        PasswordChangeDTO deserializedDto = (PasswordChangeDTO) ois.readObject();
        ois.close();

        // Verify
        assertThat(deserializedDto.getCurrentPassword()).isEqualTo(originalDto.getCurrentPassword());
        assertThat(deserializedDto.getNewPassword()).isEqualTo(originalDto.getNewPassword());
    }

    @Test
    void testSerializationWithNullValues() throws Exception {
        PasswordChangeDTO originalDto = new PasswordChangeDTO(null, null);

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalDto);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        PasswordChangeDTO deserializedDto = (PasswordChangeDTO) ois.readObject();
        ois.close();

        // Verify
        assertThat(deserializedDto.getCurrentPassword()).isNull();
        assertThat(deserializedDto.getNewPassword()).isNull();
    }

    @Test
    void testSerialVersionUID() throws NoSuchFieldException {
        assertThat(PasswordChangeDTO.class.getDeclaredField("serialVersionUID")).isNotNull();
    }
}

package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserDTOTest {

    @Test
    void testDefaultConstructor() {
        UserDTO userDTO = new UserDTO();

        assertThat(userDTO.getId()).isNull();
        assertThat(userDTO.getLogin()).isNull();
    }

    @Test
    void testConstructorWithUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getLogin()).thenReturn("testuser");

        UserDTO userDTO = new UserDTO(user);

        assertThat(userDTO.getId()).isEqualTo(1L);
        assertThat(userDTO.getLogin()).isEqualTo("testuser");
    }

    @Test
    void testConstructorWithUserNullValues() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(null);
        when(user.getLogin()).thenReturn(null);

        UserDTO userDTO = new UserDTO(user);

        assertThat(userDTO.getId()).isNull();
        assertThat(userDTO.getLogin()).isNull();
    }

    @Test
    void testGettersAndSetters() {
        UserDTO userDTO = new UserDTO();

        // Test id
        userDTO.setId(1L);
        assertThat(userDTO.getId()).isEqualTo(1L);

        // Test login
        userDTO.setLogin("testuser");
        assertThat(userDTO.getLogin()).isEqualTo("testuser");
    }

    @Test
    void testSettersWithNullValues() {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(null);
        userDTO.setLogin(null);

        assertThat(userDTO.getId()).isNull();
        assertThat(userDTO.getLogin()).isNull();
    }

    @Test
    void testEquals() {
        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setLogin("testuser");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(1L);
        userDTO2.setLogin("testuser");

        UserDTO userDTO3 = new UserDTO();
        userDTO3.setId(2L);
        userDTO3.setLogin("testuser");

        UserDTO userDTO4 = new UserDTO();
        userDTO4.setId(1L);
        userDTO4.setLogin("differentuser");

        // Test equality
        assertThat(userDTO1).isEqualTo(userDTO2);
        assertThat(userDTO1).isNotEqualTo(userDTO3);
        assertThat(userDTO1).isNotEqualTo(userDTO4);

        // Test reflexive
        assertThat(userDTO1).isEqualTo(userDTO1);

        // Test null
        assertThat(userDTO1).isNotEqualTo(null);

        // Test different class
        assertThat(userDTO1).isNotEqualTo("string");
    }

    @Test
    void testEqualsWithNullIds() {
        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(null);
        userDTO1.setLogin("testuser");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(null);
        userDTO2.setLogin("testuser");

        UserDTO userDTO3 = new UserDTO();
        userDTO3.setId(1L);
        userDTO3.setLogin("testuser");

        // When both IDs are null, they should not be equal
        assertThat(userDTO1).isNotEqualTo(userDTO2);

        // When one ID is null and the other is not, they should not be equal
        assertThat(userDTO1).isNotEqualTo(userDTO3);
    }

    @Test
    void testEqualsWithOneNullId() {
        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setLogin("testuser");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(null);
        userDTO2.setLogin("testuser");

        assertThat(userDTO1).isNotEqualTo(userDTO2);
    }

    @Test
    void testHashCode() {
        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setLogin("testuser");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(1L);
        userDTO2.setLogin("testuser");

        UserDTO userDTO3 = new UserDTO();
        userDTO3.setId(2L);
        userDTO3.setLogin("differentuser");

        // Equal objects should have equal hash codes
        assertThat(userDTO1.hashCode()).isEqualTo(userDTO2.hashCode());

        // Different objects should have different hash codes (though not guaranteed)
        assertThat(userDTO1.hashCode()).isNotEqualTo(userDTO3.hashCode());
    }

    @Test
    void testHashCodeWithNullValues() {
        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(null);
        userDTO1.setLogin(null);

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(null);
        userDTO2.setLogin(null);

        assertThat(userDTO1.hashCode()).isEqualTo(userDTO2.hashCode());
    }

    @Test
    void testToString() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setLogin("testuser");

        String toString = userDTO.toString();
        assertThat(toString).contains("UserDTO{");
        assertThat(toString).contains("id='1'");
        assertThat(toString).contains("login='testuser'");
    }

    @Test
    void testToStringWithNullValues() {
        UserDTO userDTO = new UserDTO();

        String toString = userDTO.toString();
        assertThat(toString).contains("UserDTO{");
        assertThat(toString).contains("id='null'");
        assertThat(toString).contains("login='null'");
    }

    @Test
    void testSerialization() throws Exception {
        UserDTO originalDTO = new UserDTO();
        originalDTO.setId(1L);
        originalDTO.setLogin("testuser");

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalDTO);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UserDTO deserializedDTO = (UserDTO) ois.readObject();
        ois.close();

        // Verify
        assertThat(deserializedDTO.getId()).isEqualTo(originalDTO.getId());
        assertThat(deserializedDTO.getLogin()).isEqualTo(originalDTO.getLogin());
        assertThat(deserializedDTO).isEqualTo(originalDTO);
    }

    @Test
    void testSerializationWithNullValues() throws Exception {
        UserDTO originalDTO = new UserDTO();

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalDTO);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UserDTO deserializedDTO = (UserDTO) ois.readObject();
        ois.close();

        // Verify
        assertThat(deserializedDTO.getId()).isNull();
        assertThat(deserializedDTO.getLogin()).isNull();
    }

    @Test
    void testSerialVersionUID() throws NoSuchFieldException {
        assertThat(UserDTO.class.getDeclaredField("serialVersionUID")).isNotNull();
    }

    @Test
    void testHashCodeConsistency() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setLogin("testuser");

        int hashCode1 = userDTO.hashCode();
        int hashCode2 = userDTO.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void testEqualsContract() {
        UserDTO userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setLogin("testuser");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(1L);
        userDTO2.setLogin("testuser");

        UserDTO userDTO3 = new UserDTO();
        userDTO3.setId(1L);
        userDTO3.setLogin("testuser");

        // Reflexive: x.equals(x) should be true
        assertThat(userDTO1).isEqualTo(userDTO1);

        // Symmetric: x.equals(y) should be true if y.equals(x) is true
        assertThat(userDTO1).isEqualTo(userDTO2);
        assertThat(userDTO2).isEqualTo(userDTO1);

        // Transitive: if x.equals(y) and y.equals(z), then x.equals(z)
        assertThat(userDTO1).isEqualTo(userDTO2);
        assertThat(userDTO2).isEqualTo(userDTO3);
        assertThat(userDTO1).isEqualTo(userDTO3);

        // Consistent: multiple invocations should return the same result
        assertThat(userDTO1).isEqualTo(userDTO2);
        assertThat(userDTO1).isEqualTo(userDTO2);

        // Null: x.equals(null) should be false
        assertThat(userDTO1).isNotEqualTo(null);
    }

    @Test
    void testSettersWithSpecialCharacters() {
        UserDTO userDTO = new UserDTO();

        String specialLogin = "user!@#$%^&*()";
        userDTO.setLogin(specialLogin);

        assertThat(userDTO.getLogin()).isEqualTo(specialLogin);
    }

    @Test
    void testSettersWithEmptyString() {
        UserDTO userDTO = new UserDTO();

        userDTO.setLogin("");

        assertThat(userDTO.getLogin()).isEqualTo("");
    }
}

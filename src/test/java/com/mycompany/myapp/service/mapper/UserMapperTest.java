package com.mycompany.myapp.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.domain.Authority;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.service.dto.AdminUserDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void testUsersToUserDTOs() {
        // Given
        User user1 = createTestUser(1L, "user1", "user1@example.com");
        User user2 = createTestUser(2L, "user2", "user2@example.com");
        List<User> users = Arrays.asList(user1, user2);

        // When
        List<UserDTO> userDTOs = userMapper.usersToUserDTOs(users);

        // Then
        assertThat(userDTOs).hasSize(2);
        assertThat(userDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(userDTOs.get(0).getLogin()).isEqualTo("user1");
        assertThat(userDTOs.get(1).getId()).isEqualTo(2L);
        assertThat(userDTOs.get(1).getLogin()).isEqualTo("user2");
    }

    @Test
    void testUsersToUserDTOsWithNullUser() {
        // Given
        User user1 = createTestUser(1L, "user1", "user1@example.com");
        List<User> users = Arrays.asList(user1, null);

        // When
        List<UserDTO> userDTOs = userMapper.usersToUserDTOs(users);

        // Then
        assertThat(userDTOs).hasSize(1);
        assertThat(userDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(userDTOs.get(0).getLogin()).isEqualTo("user1");
    }

    @Test
    void testUsersToUserDTOsWithEmptyList() {
        // Given
        List<User> users = Collections.emptyList();

        // When
        List<UserDTO> userDTOs = userMapper.usersToUserDTOs(users);

        // Then
        assertThat(userDTOs).isEmpty();
    }

    @Test
    void testUserToUserDTO() {
        // Given
        User user = createTestUser(1L, "testuser", "test@example.com");

        // When
        UserDTO userDTO = userMapper.userToUserDTO(user);

        // Then
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(1L);
        assertThat(userDTO.getLogin()).isEqualTo("testuser");
    }

    @Test
    void testUsersToAdminUserDTOs() {
        // Given
        User user1 = createTestUser(1L, "user1", "user1@example.com");
        User user2 = createTestUser(2L, "user2", "user2@example.com");
        List<User> users = Arrays.asList(user1, user2);

        // When
        List<AdminUserDTO> adminUserDTOs = userMapper.usersToAdminUserDTOs(users);

        // Then
        assertThat(adminUserDTOs).hasSize(2);
        assertThat(adminUserDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(adminUserDTOs.get(0).getLogin()).isEqualTo("user1");
        assertThat(adminUserDTOs.get(1).getId()).isEqualTo(2L);
        assertThat(adminUserDTOs.get(1).getLogin()).isEqualTo("user2");
    }

    @Test
    void testUsersToAdminUserDTOsWithNullUser() {
        // Given
        User user1 = createTestUser(1L, "user1", "user1@example.com");
        List<User> users = Arrays.asList(user1, null);

        // When
        List<AdminUserDTO> adminUserDTOs = userMapper.usersToAdminUserDTOs(users);

        // Then
        assertThat(adminUserDTOs).hasSize(1);
        assertThat(adminUserDTOs.get(0).getId()).isEqualTo(1L);
        assertThat(adminUserDTOs.get(0).getLogin()).isEqualTo("user1");
    }

    @Test
    void testUserToAdminUserDTO() {
        // Given
        User user = createTestUser(1L, "testuser", "test@example.com");

        // When
        AdminUserDTO adminUserDTO = userMapper.userToAdminUserDTO(user);

        // Then
        assertThat(adminUserDTO).isNotNull();
        assertThat(adminUserDTO.getId()).isEqualTo(1L);
        assertThat(adminUserDTO.getLogin()).isEqualTo("testuser");
        assertThat(adminUserDTO.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUserDTOsToUsers() {
        // Given
        AdminUserDTO userDTO1 = createTestAdminUserDTO(1L, "user1", "user1@example.com");
        AdminUserDTO userDTO2 = createTestAdminUserDTO(2L, "user2", "user2@example.com");
        List<AdminUserDTO> userDTOs = Arrays.asList(userDTO1, userDTO2);

        // When
        List<User> users = userMapper.userDTOsToUsers(userDTOs);

        // Then
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(0).getLogin()).isEqualTo("user1");
        assertThat(users.get(1).getId()).isEqualTo(2L);
        assertThat(users.get(1).getLogin()).isEqualTo("user2");
    }

    @Test
    void testUserDTOsToUsersWithNullDTO() {
        // Given
        AdminUserDTO userDTO1 = createTestAdminUserDTO(1L, "user1", "user1@example.com");
        List<AdminUserDTO> userDTOs = Arrays.asList(userDTO1, null);

        // When
        List<User> users = userMapper.userDTOsToUsers(userDTOs);

        // Then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(0).getLogin()).isEqualTo("user1");
    }

    @Test
    void testUserDTOToUser() {
        // Given
        AdminUserDTO userDTO = createTestAdminUserDTO(1L, "testuser", "test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setImageUrl("http://example.com/image.jpg");
        userDTO.setActivated(true);
        userDTO.setLangKey("en");
        userDTO.setCreatedBy("system");
        userDTO.setCreatedDate(Instant.now());
        userDTO.setLastModifiedBy("admin");
        userDTO.setLastModifiedDate(Instant.now());
        userDTO.setAuthorities(Set.of("ROLE_USER", "ROLE_ADMIN"));

        // When
        User user = userMapper.userDTOToUser(userDTO);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getLogin()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getImageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(user.isActivated()).isTrue();
        assertThat(user.getLangKey()).isEqualTo("en");
        assertThat(user.getCreatedBy()).isEqualTo("system");
        assertThat(user.getCreatedDate()).isNotNull();
        assertThat(user.getLastModifiedBy()).isEqualTo("admin");
        assertThat(user.getLastModifiedDate()).isNotNull();
        assertThat(user.getAuthorities()).hasSize(2);
        assertThat(user.getAuthorities().stream().map(Authority::getName)).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void testUserDTOToUserWithNullDTO() {
        // When
        User user = userMapper.userDTOToUser(null);

        // Then
        assertThat(user).isNull();
    }

    @Test
    void testUserDTOToUserWithNullAuthorities() {
        // Given
        AdminUserDTO userDTO = createTestAdminUserDTO(1L, "testuser", "test@example.com");
        userDTO.setAuthorities(null);

        // When
        User user = userMapper.userDTOToUser(userDTO);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void testUserDTOToUserWithEmptyAuthorities() {
        // Given
        AdminUserDTO userDTO = createTestAdminUserDTO(1L, "testuser", "test@example.com");
        userDTO.setAuthorities(Collections.emptySet());

        // When
        User user = userMapper.userDTOToUser(userDTO);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void testUserFromId() {
        // Given
        Long id = 123L;

        // When
        User user = userMapper.userFromId(id);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(123L);
        assertThat(user.getLogin()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    void testUserFromIdWithNullId() {
        // When
        User user = userMapper.userFromId(null);

        // Then
        assertThat(user).isNull();
    }

    @Test
    void testToDtoId() {
        // Given
        User user = createTestUser(1L, "testuser", "test@example.com");

        // When
        UserDTO userDTO = userMapper.toDtoId(user);

        // Then
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(1L);
        assertThat(userDTO.getLogin()).isNull(); // Should be null due to ignoreByDefault
    }

    @Test
    void testToDtoIdWithNullUser() {
        // When
        UserDTO userDTO = userMapper.toDtoId(null);

        // Then
        assertThat(userDTO).isNull();
    }

    @Test
    void testToDtoIdSet() {
        // Given
        User user1 = createTestUser(1L, "user1", "user1@example.com");
        User user2 = createTestUser(2L, "user2", "user2@example.com");
        Set<User> users = Set.of(user1, user2);

        // When
        Set<UserDTO> userDTOs = userMapper.toDtoIdSet(users);

        // Then
        assertThat(userDTOs).hasSize(2);
        assertThat(userDTOs.stream().map(UserDTO::getId)).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testToDtoIdSetWithNullSet() {
        // When
        Set<UserDTO> userDTOs = userMapper.toDtoIdSet(null);

        // Then
        assertThat(userDTOs).isEmpty();
    }

    @Test
    void testToDtoIdSetWithEmptySet() {
        // Given
        Set<User> users = Collections.emptySet();

        // When
        Set<UserDTO> userDTOs = userMapper.toDtoIdSet(users);

        // Then
        assertThat(userDTOs).isEmpty();
    }

    @Test
    void testToDtoLogin() {
        // Given
        User user = createTestUser(1L, "testuser", "test@example.com");

        // When
        UserDTO userDTO = userMapper.toDtoLogin(user);

        // Then
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(1L);
        assertThat(userDTO.getLogin()).isEqualTo("testuser");
    }

    @Test
    void testToDtoLoginWithNullUser() {
        // When
        UserDTO userDTO = userMapper.toDtoLogin(null);

        // Then
        assertThat(userDTO).isNull();
    }

    @Test
    void testToDtoLoginSet() {
        // Given
        User user1 = createTestUser(1L, "user1", "user1@example.com");
        User user2 = createTestUser(2L, "user2", "user2@example.com");
        Set<User> users = Set.of(user1, user2);

        // When
        Set<UserDTO> userDTOs = userMapper.toDtoLoginSet(users);

        // Then
        assertThat(userDTOs).hasSize(2);
        assertThat(userDTOs.stream().map(UserDTO::getId)).containsExactlyInAnyOrder(1L, 2L);
        assertThat(userDTOs.stream().map(UserDTO::getLogin)).containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void testToDtoLoginSetWithNullSet() {
        // When
        Set<UserDTO> userDTOs = userMapper.toDtoLoginSet(null);

        // Then
        assertThat(userDTOs).isEmpty();
    }

    @Test
    void testToDtoLoginSetWithEmptySet() {
        // Given
        Set<User> users = Collections.emptySet();

        // When
        Set<UserDTO> userDTOs = userMapper.toDtoLoginSet(users);

        // Then
        assertThat(userDTOs).isEmpty();
    }

    @Test
    void testToDtoIdSetWithNullUser() {
        // Given
        User user1 = createTestUser(1L, "user1", "user1@example.com");
        Set<User> users = new HashSet<>();
        users.add(user1);
        users.add(null);

        // When
        Set<UserDTO> userDTOs = userMapper.toDtoIdSet(users);

        // Then
        assertThat(userDTOs).hasSize(2); // One valid user and one null becomes null UserDTO
        assertThat(userDTOs.stream().filter(Objects::nonNull).map(UserDTO::getId)).containsExactly(1L);
    }

    @Test
    void testToDtoLoginSetWithNullUser() {
        // Given
        User user1 = createTestUser(1L, "user1", "user1@example.com");
        Set<User> users = new HashSet<>();
        users.add(user1);
        users.add(null);

        // When
        Set<UserDTO> userDTOs = userMapper.toDtoLoginSet(users);

        // Then
        assertThat(userDTOs).hasSize(2); // One valid user and one null becomes null UserDTO
        assertThat(userDTOs.stream().filter(Objects::nonNull).map(UserDTO::getId)).containsExactly(1L);
        assertThat(userDTOs.stream().filter(Objects::nonNull).map(UserDTO::getLogin)).containsExactly("user1");
    }

    private User createTestUser(Long id, String login, String email) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setEmail(email);
        user.setActivated(true);
        user.setLangKey("en");
        user.setFirstName("Test");
        user.setLastName("User");

        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        user.setAuthorities(Set.of(authority));

        return user;
    }

    private AdminUserDTO createTestAdminUserDTO(Long id, String login, String email) {
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setId(id);
        userDTO.setLogin(login);
        userDTO.setEmail(email);
        userDTO.setActivated(true);
        userDTO.setLangKey("en");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setAuthorities(Set.of("ROLE_USER"));
        return userDTO;
    }
}

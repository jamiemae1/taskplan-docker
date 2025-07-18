package com.mycompany.myapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Authority;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.UserRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Unit tests for {@link DomainUserDetailsService}.
 */
@ExtendWith(MockitoExtension.class)
class DomainUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private DomainUserDetailsService domainUserDetailsService;

    @BeforeEach
    void setUp() {
        domainUserDetailsService = new DomainUserDetailsService(userRepository);
    }

    @Test
    void testLoadUserByUsername_WithValidLogin_ShouldReturnUserDetails() {
        // Given
        String login = "testuser";
        User user = createTestUser(login, "test@example.com", true);
        when(userRepository.findOneWithAuthoritiesByLogin(login)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(login);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(login);
        assertThat(userDetails.getPassword()).isEqualTo("password");
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        verify(userRepository).findOneWithAuthoritiesByLogin(login);
        verify(userRepository, never()).findOneWithAuthoritiesByEmailIgnoreCase(anyString());
    }

    @Test
    void testLoadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // Given
        String email = "test@example.com";
        User user = createTestUser("testuser", email, true);
        when(userRepository.findOneWithAuthoritiesByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("password");
        assertThat(userDetails.getAuthorities()).hasSize(2);
        verify(userRepository).findOneWithAuthoritiesByEmailIgnoreCase(email);
        verify(userRepository, never()).findOneWithAuthoritiesByLogin(anyString());
    }

    @Test
    void testLoadUserByUsername_WithUppercaseLogin_ShouldConvertToLowercase() {
        // Given
        String login = "TESTUSER";
        String lowercaseLogin = "testuser";
        User user = createTestUser(lowercaseLogin, "test@example.com", true);
        when(userRepository.findOneWithAuthoritiesByLogin(lowercaseLogin)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(login);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(lowercaseLogin);
        verify(userRepository).findOneWithAuthoritiesByLogin(lowercaseLogin);
    }

    @Test
    void testLoadUserByUsername_WithInvalidEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        String email = "invalid@example.com";
        when(userRepository.findOneWithAuthoritiesByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(UsernameNotFoundException.class)
            .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(email))
            .withMessage("User with email " + email + " was not found in the database");
        verify(userRepository).findOneWithAuthoritiesByEmailIgnoreCase(email);
    }

    @Test
    void testLoadUserByUsername_WithInvalidLogin_ShouldThrowUsernameNotFoundException() {
        // Given
        String login = "invaliduser";
        when(userRepository.findOneWithAuthoritiesByLogin(login)).thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(UsernameNotFoundException.class)
            .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(login))
            .withMessage("User " + login + " was not found in the database");
        verify(userRepository).findOneWithAuthoritiesByLogin(login);
    }

    @Test
    void testLoadUserByUsername_WithInactivatedUser_ShouldThrowUserNotActivatedException() {
        // Given
        String login = "inactiveuser";
        User user = createTestUser(login, "inactive@example.com", false);
        when(userRepository.findOneWithAuthoritiesByLogin(login)).thenReturn(Optional.of(user));

        // When & Then
        assertThatExceptionOfType(UserNotActivatedException.class)
            .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(login))
            .withMessage("User " + login + " was not activated");
        verify(userRepository).findOneWithAuthoritiesByLogin(login);
    }

    @Test
    void testLoadUserByUsername_WithInactivatedUserByEmail_ShouldThrowUserNotActivatedException() {
        // Given
        String email = "inactive@example.com";
        User user = createTestUser("inactiveuser", email, false);
        when(userRepository.findOneWithAuthoritiesByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // When & Then
        assertThatExceptionOfType(UserNotActivatedException.class)
            .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(email))
            .withMessage("User " + email + " was not activated");
        verify(userRepository).findOneWithAuthoritiesByEmailIgnoreCase(email);
    }

    @Test
    void testLoadUserByUsername_WithUserWithoutAuthorities_ShouldReturnUserDetailsWithEmptyAuthorities() {
        // Given
        String login = "usernoauth";
        User user = createTestUser(login, "noauth@example.com", true);
        user.setAuthorities(new HashSet<>());
        when(userRepository.findOneWithAuthoritiesByLogin(login)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(login);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(login);
        assertThat(userDetails.getAuthorities()).isEmpty();
        verify(userRepository).findOneWithAuthoritiesByLogin(login);
    }

    @Test
    void testLoadUserByUsername_WithComplexEmail_ShouldHandleCorrectly() {
        // Given
        String email = "test.user+tag@example-domain.com";
        User user = createTestUser("testuser", email, true);
        when(userRepository.findOneWithAuthoritiesByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        verify(userRepository).findOneWithAuthoritiesByEmailIgnoreCase(email);
    }

    @Test
    void testLoadUserByUsername_WithNullLogin_ShouldHandleGracefully() {
        // Given
        String login = null;
        when(userRepository.findOneWithAuthoritiesByEmailIgnoreCase(login)).thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(UsernameNotFoundException.class)
            .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(login))
            .withMessage("User with email null was not found in the database");
        verify(userRepository).findOneWithAuthoritiesByEmailIgnoreCase(login);
    }

    @Test
    void testLoadUserByUsername_WithEmptyLogin_ShouldHandleCorrectly() {
        // Given
        String login = "";
        when(userRepository.findOneWithAuthoritiesByEmailIgnoreCase(login)).thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(UsernameNotFoundException.class)
            .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(login))
            .withMessage("User with email " + login + " was not found in the database");
        verify(userRepository).findOneWithAuthoritiesByEmailIgnoreCase(login);
    }

    @Test
    void testLoadUserByUsername_WithInvalidEmailFormat_ShouldTreatAsLogin() {
        // Given
        String invalidEmail = "not-an-email";
        when(userRepository.findOneWithAuthoritiesByLogin(invalidEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatExceptionOfType(UsernameNotFoundException.class)
            .isThrownBy(() -> domainUserDetailsService.loadUserByUsername(invalidEmail))
            .withMessage("User " + invalidEmail + " was not found in the database");
        verify(userRepository).findOneWithAuthoritiesByLogin(invalidEmail);
        verify(userRepository, never()).findOneWithAuthoritiesByEmailIgnoreCase(anyString());
    }

    @Test
    void testLoadUserByUsername_WithSingleAuthority_ShouldReturnCorrectAuthorities() {
        // Given
        String login = "singleauth";
        User user = createTestUser(login, "single@example.com", true);
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        authorities.add(authority);
        user.setAuthorities(authorities);
        when(userRepository.findOneWithAuthoritiesByLogin(login)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(login);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        verify(userRepository).findOneWithAuthoritiesByLogin(login);
    }

    private User createTestUser(String login, String email, boolean activated) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword("password");
        user.setActivated(activated);

        Set<Authority> authorities = new HashSet<>();
        Authority userAuthority = new Authority();
        userAuthority.setName("ROLE_USER");
        authorities.add(userAuthority);

        Authority adminAuthority = new Authority();
        adminAuthority.setName("ROLE_ADMIN");
        authorities.add(adminAuthority);

        user.setAuthorities(authorities);
        return user;
    }
}

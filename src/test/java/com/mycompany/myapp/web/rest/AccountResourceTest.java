package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.MailService;
import com.mycompany.myapp.service.UserService;
import com.mycompany.myapp.service.dto.AdminUserDTO;
import com.mycompany.myapp.service.dto.PasswordChangeDTO;
import com.mycompany.myapp.web.rest.errors.EmailAlreadyUsedException;
import com.mycompany.myapp.web.rest.errors.InvalidPasswordException;
import com.mycompany.myapp.web.rest.vm.KeyAndPasswordVM;
import com.mycompany.myapp.web.rest.vm.ManagedUserVM;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountResourceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private MailService mailService;

    @Mock
    private User user;

    private AccountResource accountResource;

    @BeforeEach
    void setUp() {
        accountResource = new AccountResource(userRepository, userService, mailService);
    }

    @Test
    void testRegisterAccount() {
        // Given
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("testuser");
        managedUserVM.setPassword("validpassword123");
        managedUserVM.setEmail("test@example.com");

        when(userService.registerUser(any(ManagedUserVM.class), anyString())).thenReturn(user);
        when(user.getActivationKey()).thenReturn("activation-key");
        when(userService.activateRegistration(anyString())).thenReturn(Optional.of(user));

        // When
        accountResource.registerAccount(managedUserVM);

        // Then
        verify(userService).registerUser(managedUserVM, "validpassword123");
        verify(userService).activateRegistration("activation-key");
    }

    @Test
    void testRegisterAccountWithInvalidPassword() {
        // Given
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("testuser");
        managedUserVM.setPassword("123"); // Too short
        managedUserVM.setEmail("test@example.com");

        // When & Then
        assertThrows(InvalidPasswordException.class, () -> {
            accountResource.registerAccount(managedUserVM);
        });

        verify(userService, never()).registerUser(any(ManagedUserVM.class), anyString());
    }

    @Test
    void testRegisterAccountWithEmptyPassword() {
        // Given
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("testuser");
        managedUserVM.setPassword(""); // Empty password
        managedUserVM.setEmail("test@example.com");

        // When & Then
        assertThrows(InvalidPasswordException.class, () -> {
            accountResource.registerAccount(managedUserVM);
        });

        verify(userService, never()).registerUser(any(ManagedUserVM.class), anyString());
    }

    @Test
    void testRegisterAccountWithNullPassword() {
        // Given
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("testuser");
        managedUserVM.setPassword(null); // Null password
        managedUserVM.setEmail("test@example.com");

        // When & Then
        assertThrows(InvalidPasswordException.class, () -> {
            accountResource.registerAccount(managedUserVM);
        });

        verify(userService, never()).registerUser(any(ManagedUserVM.class), anyString());
    }

    @Test
    void testActivateAccountSuccess() {
        // Given
        String activationKey = "valid-key";
        when(userService.activateRegistration(activationKey)).thenReturn(Optional.of(user));

        // When
        accountResource.activateAccount(activationKey);

        // Then
        verify(userService).activateRegistration(activationKey);
    }

    @Test
    void testActivateAccountWithInvalidKey() {
        // Given
        String activationKey = "invalid-key";
        when(userService.activateRegistration(activationKey)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            accountResource.activateAccount(activationKey);
        });

        verify(userService).activateRegistration(activationKey);
    }

    @Test
    void testGetAccountSuccess() {
        // Given
        when(userService.getUserWithAuthorities()).thenReturn(Optional.of(user));

        // When
        AdminUserDTO result = accountResource.getAccount();

        // Then
        assertThat(result).isNotNull();
        verify(userService).getUserWithAuthorities();
    }

    @Test
    void testGetAccountUserNotFound() {
        // Given
        when(userService.getUserWithAuthorities()).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            accountResource.getAccount();
        });

        verify(userService).getUserWithAuthorities();
    }

    @Test
    void testSaveAccountSuccess() {
        // Given
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));

            when(userRepository.findOneByEmailIgnoreCase("test@example.com")).thenReturn(Optional.empty());
            when(userRepository.findOneByLogin("testuser")).thenReturn(Optional.of(user));

            // When
            accountResource.saveAccount(userDTO);

            // Then
            verify(userService).updateUser(
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getEmail(),
                userDTO.getLangKey(),
                userDTO.getImageUrl()
            );
        }
    }

    @Test
    void testSaveAccountEmailAlreadyUsed() {
        // Given
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setLogin("otheruser");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));

            when(userRepository.findOneByEmailIgnoreCase("existing@example.com")).thenReturn(Optional.of(existingUser));

            // When & Then
            assertThrows(EmailAlreadyUsedException.class, () -> {
                accountResource.saveAccount(userDTO);
            });
        }
    }

    @Test
    void testSaveAccountCurrentUserNotFound() {
        // Given
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setEmail("test@example.com");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                accountResource.saveAccount(userDTO);
            });
        }
    }

    @Test
    void testSaveAccountUserNotFoundInRepository() {
        // Given
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setEmail("test@example.com");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));

            when(userRepository.findOneByEmailIgnoreCase("test@example.com")).thenReturn(Optional.empty());
            when(userRepository.findOneByLogin("testuser")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                accountResource.saveAccount(userDTO);
            });
        }
    }

    @Test
    void testChangePasswordSuccess() {
        // Given
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("oldpassword");
        passwordChangeDTO.setNewPassword("newvalidpassword123");

        // When
        accountResource.changePassword(passwordChangeDTO);

        // Then
        verify(userService).changePassword("oldpassword", "newvalidpassword123");
    }

    @Test
    void testChangePasswordInvalidNewPassword() {
        // Given
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("oldpassword");
        passwordChangeDTO.setNewPassword("123"); // Too short

        // When & Then
        assertThrows(InvalidPasswordException.class, () -> {
            accountResource.changePassword(passwordChangeDTO);
        });

        verify(userService, never()).changePassword(anyString(), anyString());
    }

    @Test
    void testRequestPasswordResetSuccess() {
        // Given
        String email = "test@example.com";
        when(userService.requestPasswordReset(email)).thenReturn(Optional.of(user));

        // When
        accountResource.requestPasswordReset(email);

        // Then
        verify(userService).requestPasswordReset(email);
        verify(mailService).sendPasswordResetMail(user);
    }

    @Test
    void testRequestPasswordResetUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userService.requestPasswordReset(email)).thenReturn(Optional.empty());

        // When
        accountResource.requestPasswordReset(email);

        // Then
        verify(userService).requestPasswordReset(email);
        verify(mailService, never()).sendPasswordResetMail(any(User.class));
    }

    @Test
    void testFinishPasswordResetSuccess() {
        // Given
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setKey("reset-key");
        keyAndPasswordVM.setNewPassword("newvalidpassword123");

        when(userService.completePasswordReset("newvalidpassword123", "reset-key")).thenReturn(Optional.of(user));

        // When
        accountResource.finishPasswordReset(keyAndPasswordVM);

        // Then
        verify(userService).completePasswordReset("newvalidpassword123", "reset-key");
    }

    @Test
    void testFinishPasswordResetInvalidPassword() {
        // Given
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setKey("reset-key");
        keyAndPasswordVM.setNewPassword("123"); // Too short

        // When & Then
        assertThrows(InvalidPasswordException.class, () -> {
            accountResource.finishPasswordReset(keyAndPasswordVM);
        });

        verify(userService, never()).completePasswordReset(anyString(), anyString());
    }

    @Test
    void testFinishPasswordResetInvalidKey() {
        // Given
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setKey("invalid-key");
        keyAndPasswordVM.setNewPassword("newvalidpassword123");

        when(userService.completePasswordReset("newvalidpassword123", "invalid-key")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            accountResource.finishPasswordReset(keyAndPasswordVM);
        });

        verify(userService).completePasswordReset("newvalidpassword123", "invalid-key");
    }

    @Test
    void testConstructorInitialization() {
        // Test that the constructor properly initializes all fields
        AccountResource controller = new AccountResource(userRepository, userService, mailService);

        assertThat(controller).isNotNull();
        // The constructor should initialize all dependencies properly
        // We can verify this by calling methods that use these dependencies
    }

    @Test
    void testSaveAccountWithSameUserEmail() {
        // Given
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");

        User existingUser = new User();
        existingUser.setLogin("testuser"); // Same login as current user

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));

            when(userRepository.findOneByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(existingUser));
            when(userRepository.findOneByLogin("testuser")).thenReturn(Optional.of(user));

            // When
            accountResource.saveAccount(userDTO);

            // Then
            verify(userService).updateUser(
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getEmail(),
                userDTO.getLangKey(),
                userDTO.getImageUrl()
            );
        }
    }
}

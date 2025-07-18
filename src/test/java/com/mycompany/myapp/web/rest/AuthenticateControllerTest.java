package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.web.rest.vm.LoginVM;
import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthenticateControllerTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @Mock
    private Principal principal;

    private AuthenticateController authenticateController;

    @BeforeEach
    void setUp() {
        authenticateController = new AuthenticateController(jwtEncoder, authenticationManagerBuilder);
        ReflectionTestUtils.setField(authenticateController, "tokenValidityInSeconds", 86400L);
        ReflectionTestUtils.setField(authenticateController, "tokenValidityInSecondsForRememberMe", 2592000L);
    }

    @Test
    void testAuthorize() {
        // Given
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("testuser");
        loginVM.setPassword("testpass");
        loginVM.setRememberMe(false);

        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(authentication.getName()).thenReturn("testuser");
        when(jwt.getTokenValue()).thenReturn("mock-jwt-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // When
        ResponseEntity<AuthenticateController.JWTToken> response = authenticateController.authorize(loginVM);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIdToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).contains("Bearer mock-jwt-token");

        verify(authenticationManagerBuilder).getObject();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testAuthorizeWithRememberMe() {
        // Given
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("testuser");
        loginVM.setPassword("testpass");
        loginVM.setRememberMe(true);

        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(authentication.getName()).thenReturn("testuser");
        when(jwt.getTokenValue()).thenReturn("mock-jwt-token-remember-me");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // When
        ResponseEntity<AuthenticateController.JWTToken> response = authenticateController.authorize(loginVM);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIdToken()).isEqualTo("mock-jwt-token-remember-me");
        assertThat(response.getHeaders().get(HttpHeaders.AUTHORIZATION)).contains("Bearer mock-jwt-token-remember-me");

        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testIsAuthenticatedWithPrincipal() {
        // When
        ResponseEntity<Void> response = authenticateController.isAuthenticated(principal);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testIsAuthenticatedWithoutPrincipal() {
        // When
        ResponseEntity<Void> response = authenticateController.isAuthenticated(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testCreateTokenWithoutRememberMe() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(authentication.getName()).thenReturn("testuser");
        when(jwt.getTokenValue()).thenReturn("test-jwt-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // When
        String token = authenticateController.createToken(authentication, false);

        // Then
        assertThat(token).isEqualTo("test-jwt-token");
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testCreateTokenWithRememberMe() {
        // Given
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(authentication.getName()).thenReturn("testuser");
        when(jwt.getTokenValue()).thenReturn("test-jwt-token-remember");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // When
        String token = authenticateController.createToken(authentication, true);

        // Then
        assertThat(token).isEqualTo("test-jwt-token-remember");
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testCreateTokenWithEmptyAuthorities() {
        // Given
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getName()).thenReturn("testuser");
        when(jwt.getTokenValue()).thenReturn("test-jwt-token-empty-auth");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // When
        String token = authenticateController.createToken(authentication, false);

        // Then
        assertThat(token).isEqualTo("test-jwt-token-empty-auth");
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void testJWTTokenClass() {
        // Test the inner JWTToken class
        AuthenticateController.JWTToken jwtToken = new AuthenticateController.JWTToken("test-token");

        assertThat(jwtToken.getIdToken()).isEqualTo("test-token");

        jwtToken.setIdToken("new-token");
        assertThat(jwtToken.getIdToken()).isEqualTo("new-token");
    }

    @Test
    void testJWTTokenSerialization() {
        // Test that the JWTToken can be created and accessed
        AuthenticateController.JWTToken jwtToken = new AuthenticateController.JWTToken("serialization-test");

        assertThat(jwtToken.getIdToken()).isEqualTo("serialization-test");

        // Test null token
        jwtToken.setIdToken(null);
        assertThat(jwtToken.getIdToken()).isNull();

        // Test empty token
        jwtToken.setIdToken("");
        assertThat(jwtToken.getIdToken()).isEmpty();
    }

    @Test
    void testConstructorInitialization() {
        // Test that the constructor properly initializes fields
        AuthenticateController controller = new AuthenticateController(jwtEncoder, authenticationManagerBuilder);

        assertThat(controller).isNotNull();
        // We can't test createToken directly without proper mocking, so just verify the controller is created
        // The createToken method is tested in other test methods with proper mocking
    }

    @Test
    void testAuthenticateControllerWithNullLogin() {
        // Given
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername(null);
        loginVM.setPassword("testpass");

        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        when(authentication.getName()).thenReturn("testuser");
        when(jwt.getTokenValue()).thenReturn("mock-jwt-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // When
        ResponseEntity<AuthenticateController.JWTToken> response = authenticateController.authorize(loginVM);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}

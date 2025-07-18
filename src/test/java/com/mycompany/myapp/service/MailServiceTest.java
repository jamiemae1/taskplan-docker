package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

/**
 * Unit tests for {@link MailService}.
 */
@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JHipsterProperties jHipsterProperties;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private JHipsterProperties.Mail mailProperties;

    @Mock
    private MimeMessage mimeMessage;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        lenient().when(jHipsterProperties.getMail()).thenReturn(mailProperties);
        lenient().when(mailProperties.getFrom()).thenReturn("noreply@example.com");
        lenient().when(mailProperties.getBaseUrl()).thenReturn("http://localhost:8080");
        lenient().when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService = new MailService(jHipsterProperties, javaMailSender, messageSource, templateEngine);
    }

    @Test
    void testSendEmail_WithValidParameters_ShouldSendSuccessfully() throws MessagingException {
        // Given
        String to = "user@example.com";
        String subject = "Test Subject";
        String content = "Test Content";
        boolean isMultipart = false;
        boolean isHtml = true;

        // When
        mailService.sendEmail(to, subject, content, isMultipart, isHtml);

        // Then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendEmail_WithMultipart_ShouldSendSuccessfully() throws MessagingException {
        // Given
        String to = "user@example.com";
        String subject = "Test Subject";
        String content = "Test Content";
        boolean isMultipart = true;
        boolean isHtml = false;

        // When
        mailService.sendEmail(to, subject, content, isMultipart, isHtml);

        // Then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendEmail_WithMailException_ShouldHandleGracefully() throws MessagingException {
        // Given
        String to = "user@example.com";
        String subject = "Test Subject";
        String content = "Test Content";
        boolean isMultipart = false;
        boolean isHtml = true;

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
        doThrow(new MailException("Test mail exception") {}).when(javaMailSender).send(any(MimeMessage.class));

        // When
        mailService.sendEmail(to, subject, content, isMultipart, isHtml);

        // Then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mockMessage);
        // Should not throw exception - it's handled gracefully
    }

    @Test
    void testSendEmailFromTemplate_WithValidUser_ShouldSendEmail() {
        // Given
        User user = createTestUser("testuser", "test@example.com", "en");
        String templateName = "mail/testTemplate";
        String titleKey = "email.test.title";
        String processedContent = "<html>Test Email Content</html>";
        String subject = "Test Email Subject";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(processedContent);
        when(messageSource.getMessage(eq(titleKey), eq(null), any(Locale.class))).thenReturn(subject);

        // When
        mailService.sendEmailFromTemplate(user, templateName, titleKey);

        // Then
        verify(templateEngine).process(eq(templateName), any(Context.class));
        verify(messageSource).getMessage(eq(titleKey), eq(null), any(Locale.class));
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendEmailFromTemplate_WithNullEmail_ShouldNotSendEmail() {
        // Given
        User user = createTestUser("testuser", null, "en");
        String templateName = "mail/testTemplate";
        String titleKey = "email.test.title";

        // When
        mailService.sendEmailFromTemplate(user, templateName, titleKey);

        // Then
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(messageSource, never()).getMessage(anyString(), any(), any(Locale.class));
        verify(javaMailSender, never()).createMimeMessage();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailFromTemplate_WithEmptyEmail_ShouldAttemptToSendEmail() {
        // Given
        User user = createTestUser("testuser", "", "en");
        String templateName = "mail/testTemplate";
        String titleKey = "email.test.title";
        String processedContent = "<html>Test Email Content</html>";
        String subject = "Test Email Subject";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(processedContent);
        when(messageSource.getMessage(eq(titleKey), eq(null), any(Locale.class))).thenReturn(subject);

        // When
        mailService.sendEmailFromTemplate(user, templateName, titleKey);

        // Then
        // Empty email is not null, so the service will try to send it (and fail at MimeMessageHelper level)
        verify(templateEngine).process(eq(templateName), any(Context.class));
        verify(messageSource).getMessage(eq(titleKey), eq(null), any(Locale.class));
        verify(javaMailSender).createMimeMessage();
        // The send may or may not be called depending on when the exception occurs
    }

    @Test
    void testSendEmailFromTemplate_WithDifferentLocale_ShouldUseCorrectLocale() {
        // Given
        User user = createTestUser("testuser", "test@example.com", "fr");
        String templateName = "mail/testTemplate";
        String titleKey = "email.test.title";
        String processedContent = "<html>Test Email Content</html>";
        String subject = "Test Email Subject";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(processedContent);
        when(messageSource.getMessage(eq(titleKey), eq(null), any(Locale.class))).thenReturn(subject);

        // When
        mailService.sendEmailFromTemplate(user, templateName, titleKey);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(templateName), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getLocale()).isEqualTo(Locale.forLanguageTag("fr"));

        ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
        verify(messageSource).getMessage(eq(titleKey), eq(null), localeCaptor.capture());
        assertThat(localeCaptor.getValue()).isEqualTo(Locale.forLanguageTag("fr"));
    }

    @Test
    void testSendEmailFromTemplate_WithEmptyLangKey_ShouldUseEmptyLocale() {
        // Given
        User user = createTestUser("testuser", "test@example.com", "");
        String templateName = "mail/testTemplate";
        String titleKey = "email.test.title";
        String processedContent = "<html>Test Email Content</html>";
        String subject = "Test Email Subject";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(processedContent);
        when(messageSource.getMessage(eq(titleKey), eq(null), any(Locale.class))).thenReturn(subject);

        // When
        mailService.sendEmailFromTemplate(user, templateName, titleKey);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(templateName), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getLocale()).isEqualTo(Locale.forLanguageTag(""));
    }

    @Test
    void testSendActivationEmail_WithValidUser_ShouldSendEmail() {
        // Given
        User user = createTestUser("testuser", "test@example.com", "en");
        String processedContent = "<html>Activation Email Content</html>";
        String subject = "Activation Email Subject";

        when(templateEngine.process(eq("mail/activationEmail"), any(Context.class))).thenReturn(processedContent);
        when(messageSource.getMessage(eq("email.activation.title"), eq(null), any(Locale.class))).thenReturn(subject);

        // When
        mailService.sendActivationEmail(user);

        // Then
        verify(templateEngine).process(eq("mail/activationEmail"), any(Context.class));
        verify(messageSource).getMessage(eq("email.activation.title"), eq(null), any(Locale.class));
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendActivationEmail_WithNullEmail_ShouldNotSendEmail() {
        // Given
        User user = createTestUser("testuser", null, "en");

        // When
        mailService.sendActivationEmail(user);

        // Then
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendCreationEmail_WithValidUser_ShouldSendEmail() {
        // Given
        User user = createTestUser("testuser", "test@example.com", "en");
        String processedContent = "<html>Creation Email Content</html>";
        String subject = "Creation Email Subject";

        when(templateEngine.process(eq("mail/creationEmail"), any(Context.class))).thenReturn(processedContent);
        when(messageSource.getMessage(eq("email.activation.title"), eq(null), any(Locale.class))).thenReturn(subject);

        // When
        mailService.sendCreationEmail(user);

        // Then
        verify(templateEngine).process(eq("mail/creationEmail"), any(Context.class));
        verify(messageSource).getMessage(eq("email.activation.title"), eq(null), any(Locale.class));
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendCreationEmail_WithNullEmail_ShouldNotSendEmail() {
        // Given
        User user = createTestUser("testuser", null, "en");

        // When
        mailService.sendCreationEmail(user);

        // Then
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendPasswordResetMail_WithValidUser_ShouldSendEmail() {
        // Given
        User user = createTestUser("testuser", "test@example.com", "en");
        String processedContent = "<html>Password Reset Email Content</html>";
        String subject = "Password Reset Email Subject";

        when(templateEngine.process(eq("mail/passwordResetEmail"), any(Context.class))).thenReturn(processedContent);
        when(messageSource.getMessage(eq("email.reset.title"), eq(null), any(Locale.class))).thenReturn(subject);

        // When
        mailService.sendPasswordResetMail(user);

        // Then
        verify(templateEngine).process(eq("mail/passwordResetEmail"), any(Context.class));
        verify(messageSource).getMessage(eq("email.reset.title"), eq(null), any(Locale.class));
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendPasswordResetMail_WithNullEmail_ShouldNotSendEmail() {
        // Given
        User user = createTestUser("testuser", null, "en");

        // When
        mailService.sendPasswordResetMail(user);

        // Then
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmail_WithLongContent_ShouldHandleCorrectly() throws MessagingException {
        // Given
        String to = "user@example.com";
        String subject = "Test Subject";
        String longContent = "This is a very long email content that should be handled correctly by the mail service. ".repeat(100);
        boolean isMultipart = false;
        boolean isHtml = true;

        // When
        mailService.sendEmail(to, subject, longContent, isMultipart, isHtml);

        // Then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendEmail_WithSpecialCharacters_ShouldHandleCorrectly() throws MessagingException {
        // Given
        String to = "user@example.com";
        String subject = "Test Subject with special chars: äöüß";
        String content = "Content with special characters: ñáéíóú 中文 العربية";
        boolean isMultipart = false;
        boolean isHtml = true;

        // When
        mailService.sendEmail(to, subject, content, isMultipart, isHtml);

        // Then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendEmailFromTemplate_ContextVariables_ShouldSetCorrectly() {
        // Given
        User user = createTestUser("testuser", "test@example.com", "en");
        String templateName = "mail/testTemplate";
        String titleKey = "email.test.title";
        String processedContent = "<html>Test Email Content</html>";
        String subject = "Test Email Subject";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(processedContent);
        when(messageSource.getMessage(eq(titleKey), eq(null), any(Locale.class))).thenReturn(subject);

        // When
        mailService.sendEmailFromTemplate(user, templateName, titleKey);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(templateName), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("user")).isEqualTo(user);
        assertThat(capturedContext.getVariable("baseUrl")).isEqualTo("http://localhost:8080");
    }

    @Test
    void testMailService_Constructor_ShouldInitializeCorrectly() {
        // Given
        JHipsterProperties props = mock(JHipsterProperties.class);
        JavaMailSender sender = mock(JavaMailSender.class);
        MessageSource msgSource = mock(MessageSource.class);
        SpringTemplateEngine engine = mock(SpringTemplateEngine.class);

        // When
        MailService service = new MailService(props, sender, msgSource, engine);

        // Then
        assertThat(service).isNotNull();
    }

    private User createTestUser(String login, String email, String langKey) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setLangKey(langKey);
        return user;
    }
}

package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.config.Constants;
import com.mycompany.myapp.domain.User;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MailServiceIT {

    @Mock
    private JHipsterProperties jHipsterProperties;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setup() {
        // Reset mocks
        reset(javaMailSender, jHipsterProperties, messageSource, templateEngine);

        // Setup JHipsterProperties mock
        JHipsterProperties.Mail mail = new JHipsterProperties.Mail();
        mail.setBaseUrl("http://localhost:8080");
        mail.setFrom("test@localhost");
        when(jHipsterProperties.getMail()).thenReturn(mail);

        // Mock JavaMailSender
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailFromTemplate() throws Exception {
        User user = new User();
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        user.setLangKey("en");

        when(messageSource.getMessage(any(), any(), any())).thenReturn("Test Subject");
        when(templateEngine.process(eq("mail/creationEmail"), any(Context.class))).thenReturn("<html>email content</html>");

        mailService.sendEmailFromTemplate(user, "mail/creationEmail", "email.creation.title");

        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendActivationEmail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john@example.com");

        when(messageSource.getMessage(any(), any(), any())).thenReturn("Test Subject");
        when(templateEngine.process(eq("mail/activationEmail"), any(Context.class))).thenReturn("<html>email content</html>");

        mailService.sendActivationEmail(user);

        // Wait a bit for async execution
        Thread.sleep(100);
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendCreationEmail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john@example.com");

        when(messageSource.getMessage(any(), any(), any())).thenReturn("Test Subject");
        when(templateEngine.process(eq("mail/creationEmail"), any(Context.class))).thenReturn("<html>email content</html>");

        mailService.sendCreationEmail(user);

        // Wait a bit for async execution
        Thread.sleep(100);
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendPasswordResetMail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john@example.com");

        when(messageSource.getMessage(any(), any(), any())).thenReturn("Test Subject");
        when(templateEngine.process(eq("mail/passwordResetEmail"), any(Context.class))).thenReturn("<html>email content</html>");

        mailService.sendPasswordResetMail(user);

        // Wait a bit for async execution
        Thread.sleep(100);
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false);

        verify(javaMailSender).send(any(MimeMessage.class));
    }
}

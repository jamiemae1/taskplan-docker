package com.mycompany.myapp.web.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.web.websocket.dto.ActivityDTO;
import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private StompHeaderAccessor stompHeaderAccessor;

    @Mock
    private Principal principal;

    @Mock
    private SessionDisconnectEvent disconnectEvent;

    private ActivityService activityService;

    @BeforeEach
    void setup() {
        activityService = new ActivityService(messagingTemplate);
    }

    @Test
    void testSendActivity() {
        // Setup
        ActivityDTO inputDTO = new ActivityDTO();
        inputDTO.setPage("testPage");

        String sessionId = "test-session";
        String userLogin = "testUser";
        String ipAddress = "127.0.0.1";

        when(stompHeaderAccessor.getSessionId()).thenReturn(sessionId);
        when(stompHeaderAccessor.getSessionAttributes()).thenReturn(
            new HashMap<String, Object>() {
                {
                    put("IP_ADDRESS", ipAddress);
                }
            }
        );
        when(principal.getName()).thenReturn(userLogin);

        // Execute
        ActivityDTO result = activityService.sendActivity(inputDTO, stompHeaderAccessor, principal);

        // Verify
        assertThat(result.getSessionId()).isEqualTo(sessionId);
        assertThat(result.getUserLogin()).isEqualTo(userLogin);
        assertThat(result.getIpAddress()).isEqualTo(ipAddress);
        assertThat(result.getPage()).isEqualTo("testPage");
        assertThat(result.getTime()).isNotNull();
    }

    @Test
    void testOnApplicationEvent() {
        // Setup
        String sessionId = "test-session";
        when(disconnectEvent.getSessionId()).thenReturn(sessionId);

        // Execute
        activityService.onApplicationEvent(disconnectEvent);

        // Verify
        verify(messagingTemplate).convertAndSend(eq("/topic/tracker"), any(ActivityDTO.class));
    }

    @Test
    void testActivityDTOToString() {
        // Setup
        ActivityDTO dto = new ActivityDTO();
        dto.setSessionId("testSession");
        dto.setUserLogin("testUser");
        dto.setIpAddress("127.0.0.1");
        dto.setPage("testPage");
        Instant now = Instant.now();
        dto.setTime(now);

        // Execute
        String result = dto.toString();

        // Verify
        assertThat(result).contains("testSession").contains("testUser").contains("127.0.0.1").contains("testPage").contains(now.toString());
    }

    @Test
    void testActivityDTOGettersAndSetters() {
        // Setup
        ActivityDTO dto = new ActivityDTO();
        String sessionId = "testSession";
        String userLogin = "testUser";
        String ipAddress = "127.0.0.1";
        String page = "testPage";
        Instant time = Instant.now();

        // Execute
        dto.setSessionId(sessionId);
        dto.setUserLogin(userLogin);
        dto.setIpAddress(ipAddress);
        dto.setPage(page);
        dto.setTime(time);

        // Verify
        assertThat(dto.getSessionId()).isEqualTo(sessionId);
        assertThat(dto.getUserLogin()).isEqualTo(userLogin);
        assertThat(dto.getIpAddress()).isEqualTo(ipAddress);
        assertThat(dto.getPage()).isEqualTo(page);
        assertThat(dto.getTime()).isEqualTo(time);
    }
}

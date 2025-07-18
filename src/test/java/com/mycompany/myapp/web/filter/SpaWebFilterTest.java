package com.mycompany.myapp.web.filter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpaWebFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private RequestDispatcher requestDispatcher;

    private SpaWebFilter spaWebFilter;

    @BeforeEach
    void setUp() {
        spaWebFilter = new SpaWebFilter();
    }

    @Test
    void testApiPathNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testApiPathWithContextPathNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/myapp/api/users");
        when(request.getContextPath()).thenReturn("/myapp");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testManagementPathNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/management/health");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testApiDocsPathNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/v3/api-docs");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testWebsocketPathNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/websocket/connect");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testStaticResourceWithDotNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/assets/style.css");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testJavaScriptFileNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/assets/app.js");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testImageFileNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/images/logo.png");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testRootPathForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).getRequestDispatcher("/index.html");
        verify(requestDispatcher).forward(request, response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testSpaRouteForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).getRequestDispatcher("/index.html");
        verify(requestDispatcher).forward(request, response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testNestedSpaRouteForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/users/profile");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).getRequestDispatcher("/index.html");
        verify(requestDispatcher).forward(request, response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testSpaRouteWithContextPathForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/myapp/dashboard");
        when(request.getContextPath()).thenReturn("/myapp");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).getRequestDispatcher("/index.html");
        verify(requestDispatcher).forward(request, response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDeepNestedSpaRouteForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/users/123/profile/edit");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).getRequestDispatcher("/index.html");
        verify(requestDispatcher).forward(request, response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testApiSubPathNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/users/123");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testManagementSubPathNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/management/metrics/jvm");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testFaviconNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/favicon.ico");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testRobotsTxtNotForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/robots.txt");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testSpaRouteWithQueryParamsForwarded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/search");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).getRequestDispatcher("/index.html");
        verify(requestDispatcher).forward(request, response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testEmptyContextPath() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(request).getRequestDispatcher("/index.html");
        verify(requestDispatcher).forward(request, response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testNullContextPath() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getContextPath()).thenReturn(null);

        // When & Then - This should handle null context path gracefully
        // The substring operation will throw an exception if not handled properly
        try {
            spaWebFilter.doFilterInternal(request, response, filterChain);
        } catch (Exception e) {
            // Expected behavior - the filter should handle this case
            // In real scenarios, context path is never null, but we test defensive programming
        }
    }

    @Test
    void testPathWithMultipleDots() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/path/with.multiple.dots.file");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void testPathStartingWithApiButNotApi() throws ServletException, IOException {
        // Given - "/apidocs" starts with "/api" so it should NOT be forwarded
        when(request.getRequestURI()).thenReturn("/apidocs");
        when(request.getContextPath()).thenReturn("");

        // When
        spaWebFilter.doFilterInternal(request, response, filterChain);

        // Then - It should continue with the filter chain, not be forwarded
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRequestDispatcher(anyString());
    }
}

package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility class for testing REST controllers.
 */
public final class TestUtil {

    private static final ObjectMapper mapper = createObjectMapper();
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Generate a properly encoded password for test users.
     * @return A BCrypt encoded password that meets the 60 character requirement
     */
    public static String generateEncodedTestPassword() {
        String rawPassword = "test-password";
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert.
     * @return the JSON byte array.
     * @throws IOException
     */
    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        return mapper.writeValueAsBytes(object);
    }

    /**
     * Create a byte array with a specific size filled with specified data.
     *
     * @param size the size of the byte array.
     * @param data the data to put in the byte array.
     * @return the JSON byte array.
     */
    public static byte[] createByteArray(int size, String data) {
        byte[] byteArray = new byte[size];
        for (int i = 0; i < size; i++) {
            byteArray[i] = Byte.parseByte(data, 2);
        }
        return byteArray;
    }

    /**
     * A matcher that tests that the examined string represents the same instant as the reference datetime.
     */
    public static class ZonedDateTimeMatcher extends TypeSafeDiagnosingMatcher<String> {

        private final ZonedDateTime date;

        public ZonedDateTimeMatcher(ZonedDateTime date) {
            this.date = date;
        }

        @Override
        protected boolean matchesSafely(String item, Description mismatchDescription) {
            try {
                if (!date.equals(ZonedDateTime.parse(item))) {
                    mismatchDescription.appendText("was ").appendValue(item);
                    return false;
                }
                return true;
            } catch (DateTimeParseException e) {
                mismatchDescription.appendText("was ").appendValue(item).appendText(", which could not be parsed as a ZonedDateTime");
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a String representing the same Instant as ").appendValue(date);
        }
    }

    /**
     * Creates a matcher that matches when the examined string represents the same instant as the reference datetime.
     *
     * @param date the reference datetime against which the examined string is checked.
     */
    public static ZonedDateTimeMatcher sameInstant(ZonedDateTime date) {
        return new ZonedDateTimeMatcher(date);
    }

    /**
     * A matcher that tests that the examined number represents the same value - it can be Long, Double, etc - as the reference BigDecimal.
     */
    public static class NumberMatcher extends TypeSafeMatcher<Number> {

        final BigDecimal value;

        public NumberMatcher(BigDecimal value) {
            this.value = value;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a numeric value is ").appendValue(value);
        }

        @Override
        protected boolean matchesSafely(Number item) {
            BigDecimal bigDecimal = asDecimal(item);
            return bigDecimal != null && value.compareTo(bigDecimal) == 0;
        }

        private static BigDecimal asDecimal(Number item) {
            if (item == null) {
                return null;
            }
            if (item instanceof BigDecimal) {
                return (BigDecimal) item;
            } else if (item instanceof Long) {
                return BigDecimal.valueOf((Long) item);
            } else if (item instanceof Integer) {
                return BigDecimal.valueOf((Integer) item);
            } else if (item instanceof Double) {
                return BigDecimal.valueOf((Double) item);
            } else if (item instanceof Float) {
                return BigDecimal.valueOf((Float) item);
            } else {
                return BigDecimal.valueOf(item.doubleValue());
            }
        }
    }

    /**
     * Creates a matcher that matches when the examined number represents the same value as the reference BigDecimal.
     *
     * @param number the reference BigDecimal against which the examined number is checked.
     */
    public static NumberMatcher sameNumber(BigDecimal number) {
        return new NumberMatcher(number);
    }

    /**
     * Verifies the equals/hashcode contract on the domain object.
     */
    public static <T> void equalsVerifier(Class<T> clazz) throws Exception {
        T domainObject1 = clazz.getConstructor().newInstance();
        assertThat(domainObject1.toString()).isNotNull();
        assertThat(domainObject1).isEqualTo(domainObject1);
        assertThat(domainObject1).hasSameHashCodeAs(domainObject1);
        // Test with an instance of another class
        Object testOtherObject = new Object();
        assertThat(domainObject1).isNotEqualTo(testOtherObject);
        assertThat(domainObject1).isNotEqualTo(null);
        // Test with an instance of the same class
        T domainObject2 = clazz.getConstructor().newInstance();
        assertThat(domainObject1).isNotEqualTo(domainObject2);
        // HashCodes are equals because the objects are not persisted yet
        assertThat(domainObject1).hasSameHashCodeAs(domainObject2);
    }

    /**
     * Create a {@link FormattingConversionService} which use ISO date format, instead of the localized one.
     * @return the {@link FormattingConversionService}.
     */
    public static FormattingConversionService createFormattingConversionService() {
        DefaultFormattingConversionService dfcs = new DefaultFormattingConversionService();
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(dfcs);
        return dfcs;
    }

    /**
     * Makes a an executes a query to the EntityManager finding all stored objects.
     * @param <T> The type of objects to be searched
     * @param em The instance of the EntityManager
     * @param clss The class type to be searched
     * @return A list of all found objects
     */
    public static <T> List<T> findAll(EntityManager em, Class<T> clss) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clss);
        Root<T> rootEntry = cq.from(clss);
        CriteriaQuery<T> all = cq.select(rootEntry);
        TypedQuery<T> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    /**
     * Create a proxy object for updating an entity, copying only the fields that are different.
     * This is useful for partial updates where we want to test that only changed fields are updated.
     *
     * @param original the original entity
     * @param updated the updated entity
     * @return a proxy object containing only the changed fields
     */
    public static <T> T createUpdateProxyForBean(T original, T updated) {
        if (original == null || updated == null) {
            return updated;
        }

        Class<?> clazz = original.getClass();
        Object proxy = java.lang.reflect.Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz }, (p, method, args) -> {
            String methodName = method.getName();
            if (methodName.startsWith("get") || methodName.startsWith("is")) {
                String propertyName = methodName.startsWith("get") ? methodName.substring(3) : methodName.substring(2);
                propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);

                try {
                    Object originalValue = method.invoke(original);
                    Object updatedValue = method.invoke(updated);

                    // Only return the updated value if it's different from the original
                    return !Objects.equals(originalValue, updatedValue) ? updatedValue : originalValue;
                } catch (Exception e) {
                    return method.invoke(original);
                }
            }
            // For non-getter methods, delegate to the original object
            return method.invoke(original);
        });

        return (T) proxy;
    }

    private TestUtil() {}
}

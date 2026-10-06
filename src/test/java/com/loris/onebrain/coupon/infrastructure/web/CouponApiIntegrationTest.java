package com.loris.onebrain.coupon.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;
import com.loris.onebrain.coupon.application.CouponConcurrentModificationException;
import com.loris.onebrain.coupon.application.port.CouponRepository;
import com.loris.onebrain.coupon.infrastructure.persistence.SpringDataCouponRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Coupon API (end to end)")
class CouponApiIntegrationTest {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String FUTURE = "2030-11-04T17:14:45.180Z";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataCouponRepository springDataRepository;

    @MockitoSpyBean
    private CouponRepository couponRepository;

    @Nested
    @DisplayName("POST /coupon")
    class Create {

        @Test
        @DisplayName("RN-02 / RN-09: creates a coupon with a sanitized code, ACTIVE and not redeemed")
        void createsCoupon() throws Exception {
            createCoupon(body("ABC-123", "Black Friday", "0.8", FUTURE, "false"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", matchesPattern(".*/coupon/[0-9a-f-]{36}$")))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isString())
                    .andExpect(jsonPath("$.code").value("ABC123"))
                    .andExpect(jsonPath("$.description").value("Black Friday"))
                    .andExpect(jsonPath("$.discountValue").value(0.8))
                    .andExpect(jsonPath("$.expirationDate").value(FUTURE))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.published").value(false))
                    .andExpect(jsonPath("$.redeemed").value(false));
        }

        @Test
        @DisplayName("the Location header points to the created coupon")
        void locationPointsToCreatedCoupon() throws Exception {
            String location = createCoupon(body("ABC-123", "Black Friday", "0.8", FUTURE, "true"))
                    .andReturn().getResponse().getHeader("Location");

            mockMvc.perform(get(location))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("ABC123"))
                    .andExpect(jsonPath("$.published").value(true));
        }

        @Test
        @DisplayName("RN-08: published defaults to false when omitted")
        void publishedDefaultsToFalse() throws Exception {
            createCoupon("""
                    {"code":"ABC123","description":"Black Friday","discountValue":0.8,"expirationDate":"%s"}
                    """.formatted(FUTURE))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.published").value(false));
        }

        @Test
        @DisplayName("RN-04 / RN-05: accents are normalized and the code is uppercased")
        void normalizesAccentsAndCase() throws Exception {
            createCoupon(body("café-1ß2", "Black Friday", "0.8", FUTURE, "false"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("CAFE12"));
        }

        @Test
        @DisplayName("RN-06: the minimum 0.5 is accepted and there is no maximum")
        void acceptsDiscountBoundaries() throws Exception {
            createCoupon(body("ABC123", "Black Friday", "0.5", FUTURE, "false"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.discountValue").value(0.5));
            createCoupon(body("ABC123", "Black Friday", "1000000", FUTURE, "false"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.discountValue").value(1000000));
        }

        @Test
        @DisplayName("D-07: the same code can be used by more than one coupon")
        void allowsDuplicatedCodes() throws Exception {
            createCoupon(body("ABC123", "First", "0.8", FUTURE, "false")).andExpect(status().isCreated());
            createCoupon(body("abc-123", "Second", "0.8", FUTURE, "false")).andExpect(status().isCreated());
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
                "missing code               | null       | 'Black Friday' | 0.8  | '" + FUTURE + "'               | COUPON_CODE_REQUIRED",
                "short code after sanitize  | 'AB-12'    | 'Black Friday' | 0.8  | '" + FUTURE + "'               | COUPON_CODE_INVALID_LENGTH",
                "long code                  | 'ABC1234'  | 'Black Friday' | 0.8  | '" + FUTURE + "'               | COUPON_CODE_INVALID_LENGTH",
                "only special characters    | '@#$%&*'   | 'Black Friday' | 0.8  | '" + FUTURE + "'               | COUPON_CODE_INVALID_LENGTH",
                "blank description          | 'ABC123'   | '   '          | 0.8  | '" + FUTURE + "'               | COUPON_DESCRIPTION_REQUIRED",
                "missing description        | 'ABC123'   | null           | 0.8  | '" + FUTURE + "'               | COUPON_DESCRIPTION_REQUIRED",
                "missing discount           | 'ABC123'   | 'Black Friday' | null | '" + FUTURE + "'               | COUPON_DISCOUNT_VALUE_REQUIRED",
                "discount below minimum     | 'ABC123'   | 'Black Friday' | 0.49 | '" + FUTURE + "'               | COUPON_DISCOUNT_VALUE_BELOW_MINIMUM",
                "negative discount          | 'ABC123'   | 'Black Friday' | -1   | '" + FUTURE + "'               | COUPON_DISCOUNT_VALUE_BELOW_MINIMUM",
                "missing expiration date    | 'ABC123'   | 'Black Friday' | 0.8  | null                           | COUPON_EXPIRATION_DATE_REQUIRED",
                "expiration date in past    | 'ABC123'   | 'Black Friday' | 0.8  | '2020-01-01T00:00:00Z'         | COUPON_EXPIRATION_DATE_IN_PAST"
        })
        @DisplayName("RN-01 / RN-03 / RN-06 / RN-07 / RN-14 / D-06: business rule violations return 422")
        void rejectsBusinessRuleViolations(String scenario, String code, String description, String discount,
                                           String expirationDate, String expectedCode) throws Exception {
            long before = springDataRepository.count();

            createCoupon(body(code, description, discount, expirationDate, "false"))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.title").value("Unprocessable Content"))
                    .andExpect(jsonPath("$.instance").value("/coupon"))
                    .andExpect(jsonPath("$.code").value(expectedCode))
                    .andExpect(jsonPath("$.detail").isString());

            assertThat(springDataRepository.count()).isEqualTo(before);
        }

        @Test
        @DisplayName("RN-03: the detail tells the client that 6 alphanumeric characters are required")
        void explainsInvalidCodeLength() throws Exception {
            createCoupon(body("AB-12", "Black Friday", "0.8", FUTURE, "false"))
                    .andExpect(jsonPath("$.detail").value(
                            "Coupon code must contain exactly 6 alphanumeric characters after removing special characters."));
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
                "malformed JSON             | '{\"code\": \"ABC123\",'",
                "discount is not a number   | '{\"code\":\"ABC123\",\"description\":\"d\",\"discountValue\":\"abc\",\"expirationDate\":\"" + FUTURE + "\"}'",
                "invalid date format        | '{\"code\":\"ABC123\",\"description\":\"d\",\"discountValue\":0.8,\"expirationDate\":\"tomorrow\"}'",
                "published is not a boolean | '{\"code\":\"ABC123\",\"description\":\"d\",\"discountValue\":0.8,\"expirationDate\":\"" + FUTURE + "\",\"published\":\"maybe\"}'",
                "empty body                 | ''"
        })
        @DisplayName("D-06: unreadable payloads return 400")
        void rejectsMalformedPayload(String scenario, String payload) throws Exception {
            createCoupon(payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                    .andExpect(jsonPath("$.detail").value("Request body is malformed or has invalid field types."));
        }
    }

    @Nested
    @DisplayName("GET /coupon/{id}")
    class Get {

        @Test
        void returnsCoupon() throws Exception {
            String id = createdCouponId();

            mockMvc.perform(get("/coupon/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.code").value("ABC123"))
                    .andExpect(jsonPath("$.discountValue").value(0.8))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void returns404ForUnknownCoupon() throws Exception {
            String unknownId = UUID.randomUUID().toString();

            mockMvc.perform(get("/coupon/{id}", unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").value("COUPON_NOT_FOUND"))
                    .andExpect(jsonPath("$.detail").value("Coupon not found."))
                    .andExpect(jsonPath("$.instance").value("/coupon/" + unknownId));
        }

        @Test
        @DisplayName("D-06: an invalid UUID returns 400")
        void returns400ForInvalidId() throws Exception {
            mockMvc.perform(get("/coupon/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                    .andExpect(jsonPath("$.detail").value("Path parameter has an invalid format."));
        }
    }

    @Nested
    @DisplayName("DELETE /coupon/{id}")
    class Delete {

        @Test
        @DisplayName("RN-11 / RN-13: soft delete keeps the coupon queryable with status DELETED")
        void softDeletesCoupon() throws Exception {
            String id = createdCouponId();

            mockMvc.perform(delete("/coupon/{id}", id))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            mockMvc.perform(get("/coupon/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DELETED"))
                    .andExpect(jsonPath("$.code").value("ABC123"))
                    .andExpect(jsonPath("$.description").value("Black Friday"))
                    .andExpect(jsonPath("$.discountValue").value(0.8))
                    .andExpect(jsonPath("$.expirationDate").value(FUTURE));
            assertThat(springDataRepository.findById(UUID.fromString(id)))
                    .hasValueSatisfying(entity -> assertThat(entity.getDeletedAt()).isNotNull());
        }

        @Test
        @DisplayName("RN-10: a published coupon can be deleted")
        void deletesPublishedCoupon() throws Exception {
            String id = JsonPath.read(createCoupon(body("ABC123", "Black Friday", "0.8", FUTURE, "true"))
                    .andReturn().getResponse().getContentAsString(), "$.id");

            mockMvc.perform(delete("/coupon/{id}", id)).andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("RN-12 / D-17: deleting twice returns 409")
        void rejectsDeletingTwice() throws Exception {
            String id = createdCouponId();
            mockMvc.perform(delete("/coupon/{id}", id)).andExpect(status().isNoContent());

            mockMvc.perform(delete("/coupon/{id}", id))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").value("COUPON_ALREADY_DELETED"))
                    .andExpect(jsonPath("$.detail").value("Coupon is already deleted."))
                    .andExpect(jsonPath("$.instance", endsWith("/coupon/" + id)));
        }

        @Test
        @DisplayName("D-13: a concurrent modification returns 409")
        void returns409OnConcurrentModification() throws Exception {
            String id = createdCouponId();
            doThrow(new CouponConcurrentModificationException(UUID.fromString(id)))
                    .when(couponRepository).save(any());

            mockMvc.perform(delete("/coupon/{id}", id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("COUPON_CONCURRENT_MODIFICATION"))
                    .andExpect(jsonPath("$.detail").value("Coupon was modified concurrently. Retry the operation."));
        }

        @Test
        void returns404ForUnknownCoupon() throws Exception {
            mockMvc.perform(delete("/coupon/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("COUPON_NOT_FOUND"));
        }

        @Test
        void returns400ForInvalidId() throws Exception {
            mockMvc.perform(delete("/coupon/{id}", "123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        }
    }

    @Test
    @DisplayName("the OpenAPI document is published")
    void exposesOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Coupon API"))
                .andExpect(jsonPath("$.paths['/coupon']").exists())
                .andExpect(jsonPath("$.paths['/coupon/{id}']").exists());
    }

    private ResultActions createCoupon(String json) throws Exception {
        return mockMvc.perform(post("/coupon").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String createdCouponId() throws Exception {
        String response = createCoupon(body("ABC-123", "Black Friday", "0.8", FUTURE, "false"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private static String body(String code, String description, String discount, String expirationDate,
                               String published) {
        return """
                {"code":%s,"description":%s,"discountValue":%s,"expirationDate":%s,"published":%s}
                """.formatted(quoted(code), quoted(description), discount, quoted(expirationDate), published);
    }

    private static String quoted(String value) {
        return value == null || "null".equals(value) ? "null" : "\"" + value + "\"";
    }
}

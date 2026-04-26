package com.cms.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Focused tests for GlobalExceptionHandler mappings.
 * Added today so yesterday's exception work stays protected as controllers grow.
 */
@WebMvcTest(controllers = GlobalExceptionHandlerTest.ExceptionProbeController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // Keeps spring test slicing happy in this codebase where security is present.
    @MockBean
    private com.cms.service.UserService userService;

    /**
     * Verifies IllegalArgumentException maps to 400 with structured payload.
     */
    @Test
    void illegalArgumentIsMappedToBadRequest() throws Exception {
        mockMvc.perform(get("/probe/illegal-argument"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("probe illegal argument"))
            .andExpect(jsonPath("$.path").value("/probe/illegal-argument"));
    }

    /**
     * Verifies ResourceNotFoundException maps to 404 with expected message.
     */
    @Test
    void resourceNotFoundIsMappedToNotFound() throws Exception {
        mockMvc.perform(get("/probe/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("probe not found"))
            .andExpect(jsonPath("$.path").value("/probe/not-found"));
    }

    /**
     * Verifies validation errors stay on 400 and include the field name.
     */
    @Test
    void validationErrorIsMappedToBadRequest() throws Exception {
        mockMvc.perform(post("/probe/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name")))
            .andExpect(jsonPath("$.path").value("/probe/validation"));
    }

    @RestController
    static class ExceptionProbeController {

        @GetMapping("/probe/illegal-argument")
        String illegalArgument() {
            throw new IllegalArgumentException("probe illegal argument");
        }

        @GetMapping("/probe/not-found")
        String notFound() {
            throw new ResourceNotFoundException("probe not found");
        }

        @PostMapping("/probe/validation")
        String validation(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody ProbeRequest request) {
            return "ok";
        }
    }

    static class ProbeRequest {

        @jakarta.validation.constraints.NotBlank(message = "name is required")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

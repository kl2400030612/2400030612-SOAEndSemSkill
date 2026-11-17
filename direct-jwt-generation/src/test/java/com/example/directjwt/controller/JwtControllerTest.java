package com.example.directjwt.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class JwtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generateReturns200WithThreePartToken() throws Exception {
        mockMvc.perform(post("/jwt/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Afreen\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(org.hamcrest.Matchers.matchesPattern("^[^.]+\\.[^.]+\\.[^.]+$")))
                .andExpect(jsonPath("$.username").value("Afreen"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void missingRoleReturns400() throws Exception {
        mockMvc.perform(post("/jwt/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Afreen\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.role").exists());
    }

    @Test
    void blankUsernameReturns400() throws Exception {
        mockMvc.perform(post("/jwt/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"  \",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/jwt/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyGarbageTokenReturns401() throws Exception {
        mockMvc.perform(post("/jwt/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc.def.ghi\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyMissingTokenReturns400() throws Exception {
        mockMvc.perform(post("/jwt/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

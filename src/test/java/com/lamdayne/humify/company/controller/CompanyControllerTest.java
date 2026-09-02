package com.lamdayne.humify.company.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamdayne.humify.auth.security.filter.JwtAuthenticationFilter;
import com.lamdayne.humify.common.util.SqidsUtil;
import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompanyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;
    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    SqidsUtil sqidsUtil;

    private CreateCompanyRequest request;
    private CompanyResponse companyResponse;

    @BeforeEach
    void initData() {

        request = new CreateCompanyRequest();

        setField(request, "name", "FPT Software");
        setField(request, "field", "Software");
        setField(request, "website", "https://fpt.com");
        setField(request, "taxCode", "123456789");
        setField(request, "phone", "0901234567");
        setField(request, "email", "contact@fpt.com");

        companyResponse = CompanyResponse.builder()
                .companyCode("COM001")
                .name("FPT Software")
                .field("Software")
                .website("https://fpt.com")
                .taxCode("123456789")
                .phone("0901234567")
                .email("contact@fpt.com")
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void createCompany_validRequest_success() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        String content = objectMapper.writeValueAsString(request);

        Mockito.when(
                companyService.createCompany(
                        ArgumentMatchers.any(CreateCompanyRequest.class)
                )
        ).thenReturn(companyResponse);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyCode").value("COM001"))
                .andExpect(jsonPath("$.data.name").value("FPT Software"))
                .andExpect(jsonPath("$.data.email").value("contact@fpt.com"));
    }

    @Test
    void createCompany_nameBlank_fail() throws Exception {

        setField(request, "name", "");

        ObjectMapper objectMapper = new ObjectMapper();

        String content = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCompany_emailInvalid_fail() throws Exception {

        setField(request, "email", "abcxyz");

        ObjectMapper objectMapper = new ObjectMapper();

        String content = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCompany_taxCodeMissing_fail() throws Exception {

        setField(request, "taxCode", "");

        ObjectMapper objectMapper = new ObjectMapper();

        String content = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
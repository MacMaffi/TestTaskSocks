package org.example.socksproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.socksproject.dto.SocksDto;
import org.example.socksproject.helper.DtoGeneratorHelper;
import org.example.socksproject.service.SocksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SocksControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @InjectMocks
    SocksController socksController;

    @Mock
    SocksService socksService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(socksController).build();
    }

    @Test
    void testIncomeSocks() throws Exception {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        mockMvc.perform(post("api/socks/income")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Socks income successfully"));

        verify(socksService).incomeSocks(any(SocksDto.class));
    }

    @Test
    void testOutcomeSocks() throws Exception {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        mockMvc.perform(post("/api/socks/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Socks outcome successfully"));

        verify(socksService).outcomeSocks(any(SocksDto.class));
    }

    @Test
    void testOutcomeSocksInsufficientQuantity() throws Exception {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        doThrow(new IllegalArgumentException("Not enough socks")).when(socksService).outcomeSocks(any(SocksDto.class));

        mockMvc.perform(post("/api/socks/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not enough socks"));
    }

    @Test
    void testCountSocks() throws Exception {
        when(socksService.getSocksCount("red", "moreThan", 50)).thenReturn(150);

        mockMvc.perform(get("/api/socks")
                        .param("color", "red")
                        .param("comparison", "moreThan")
                        .param("cottonPercentage", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string("150"));

        verify(socksService).getSocksCount("red", "moreThan", 50);
    }

    @Test
    void testCountSocksInvalidComparison() throws Exception {
        mockMvc.perform(get("/api/socks")
                        .param("color", "red")
                        .param("comparison", "invalid")
                        .param("cottonPercentage", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid comparison operator"));
    }

    @Test
    void testUpdateSocks() throws Exception {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        mockMvc.perform(put("/api/socks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Socks updated successfully"));

        verify(socksService).updateSocks(1L, any(SocksDto.class));
    }

    @Test
    void testUpdateSocksNotFound() throws Exception {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        doThrow(new IllegalArgumentException("Socks not found")).when(socksService).updateSocks(eq(1L), any(SocksDto.class));

        mockMvc.perform(put("/api/socks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Socks not found"));
    }

    @Test
    void testUploadBatch() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "socks.csv", "text/csv",
                "color,cottonPercentage,quantity\nred,50,100\nblue,70,150".getBytes());

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Batch upload successful"));

        verify(socksService).uploadFile(any());
    }

    @Test
    void testUploadBatchInvalidFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "socks.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }
}

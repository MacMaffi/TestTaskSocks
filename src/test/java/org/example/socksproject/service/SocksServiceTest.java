package org.example.socksproject.service;

import org.example.socksproject.dto.SocksDto;
import org.example.socksproject.helper.DtoGeneratorHelper;
import org.example.socksproject.model.Socks;
import org.example.socksproject.repository.SocksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocksServiceImplTest {

    @Mock
    private SocksRepository socksRepository;

    @InjectMocks
    private SocksService socksService;

    private Socks existingSocks;

    @BeforeEach
    void setUp() {
        existingSocks = DtoGeneratorHelper.generateSocks();
    }

    @Test
    void testIncomeSocksWhenSocksExist() {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        when(socksRepository.findByColorAndCottonPercentage("black", 50))
                .thenReturn(Optional.of(existingSocks));

        socksService.incomeSocks(socksDto);

        verify(socksRepository).save(any(Socks.class));
        assertEquals(20, existingSocks.getQuantity());
    }

    @Test
    void testIncomeSocksWhenSocksDoNotExist() {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        when(socksRepository.findByColorAndCottonPercentage("black", 50))
                .thenReturn(Optional.empty());

        socksService.incomeSocks(socksDto);

        verify(socksRepository).save(argThat(socks ->
                socks.getColor().equals("black") &&
                        socks.getCottonPercentage() == 50 &&
                        socks.getQuantity() == 10
        ));
    }

    @Test
    void testOutcomeSocksWhenSufficientQuantity() {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        when(socksRepository.findByColorAndCottonPercentage("black", 50))
                .thenReturn(Optional.of(existingSocks));

        socksService.outcomeSocks(socksDto);

        verify(socksRepository).save(any(Socks.class));
        assertEquals(0, existingSocks.getQuantity());
    }

    @Test
    void testOutcomeSocksWhenInsufficientQuantity() {
        SocksDto socksDto = SocksDto.builder()
                .color("black")
                .cottonPercentage(50)
                .quantity(20)
                .build();

        when(socksRepository.findByColorAndCottonPercentage("black", 50))
                .thenReturn(Optional.of(existingSocks));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            socksService.outcomeSocks(socksDto);
        });

        assertEquals("Not enough socks", exception.getMessage());
        verify(socksRepository, never()).save(any(Socks.class));
    }

    @Test
    void testOutcomeSocksWhenSocksNotFound() {
        SocksDto socksDto = DtoGeneratorHelper.generateSocksDto();

        when(socksRepository.findByColorAndCottonPercentage("black", 50))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            socksService.outcomeSocks(socksDto);
        });

        assertEquals("No socks with parameters", exception.getMessage());
    }

    @Test
    void testGetSocksCount() {
        when(socksRepository.countSocksByColorAndCottonPercentageGreaterThan("black", 50))
                .thenReturn(100);

        int count = socksService.getSocksCount("black", "moreThan", 50);

        assertEquals(100, count);
        verify(socksRepository).countSocksByColorAndCottonPercentageGreaterThan("black", 50);
    }

    @Test
    void testGetSocksCountInvalidComparison() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            socksService.getSocksCount("black", "invalid", 50);
        });

        assertEquals("Invalid comparison", exception.getMessage());
    }

    @Test
    void testUploadFileValidFile() throws Exception {
        String csvContent = "color,cottonPercentage,quantity\nblack,50,10\nred,70,20";
        MultipartFile file = mock(MultipartFile.class);

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csvContent.getBytes()));

        socksService.uploadFile(file);

        verify(socksRepository, times(2)).save(any(Socks.class));
    }

    @Test
    void testUploadFileEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            socksService.uploadFile(file);
        });

        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void testUploadFileInvalidData() throws Exception {
        String csvContent = "color,cottonPercentage,quantity\nblack,invalid,10";
        MultipartFile file = mock(MultipartFile.class);

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csvContent.getBytes()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            socksService.uploadFile(file);
        });

        assertTrue(exception.getMessage().contains("Invalid data in CSV file"));
    }
}


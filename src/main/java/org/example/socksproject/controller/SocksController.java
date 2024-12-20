package org.example.socksproject.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socksproject.dto.SocksDto;
import org.example.socksproject.service.SocksService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("api/socks")
@RequiredArgsConstructor
public class SocksController {

    private final SocksService socksService;

    @Operation(summary = "Register incoming socks", description = "Registers new socks.")
    @ApiResponse(responseCode = "200", description = "Socks income successfully registered")
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = String.class)))
    @PostMapping("/income")
    public ResponseEntity<String> incomeSocks(@RequestBody @Valid SocksDto socksDto) {
        log.info("Processing income request: {}", socksDto);
        socksService.incomeSocks(socksDto);
        log.info("Income request processed successfully for {}", socksDto);
        return ResponseEntity.ok("Socks income successfully");
    }

    @Operation(summary = "Register outgoing socks", description = "Decreases the quantity of socks.")
    @ApiResponse(responseCode = "200", description = "Socks outcome successfully registered")
    @ApiResponse(responseCode = "400", description = "Invalid input or insufficient socks")
    @PostMapping("/outcome")
    public ResponseEntity<String> outcomeSocks(@RequestBody @Valid SocksDto socksDto) {
        log.info("Processing outcome request: {}", socksDto);
        socksService.outcomeSocks(socksDto);
        log.info("Outcome request processed successfully for {}", socksDto);
        return ResponseEntity.ok("Socks outcome successfully");
    }

    @Operation(summary = "Get count of socks", description = "Gets the total count of socks")
    @ApiResponse(responseCode = "200", description = "Socks count retrieved successfully")
    @GetMapping
    public ResponseEntity<Integer> countSocks(@RequestParam String color,
                                              @RequestParam String comparison,
                                              @RequestParam int cottonPercentage) {
        log.info("Processing count request: color={}, comparison={}, cottonPercentage={}",
                color, comparison, cottonPercentage);
        int count = socksService.getSocksCount(color, comparison, cottonPercentage);
        log.info("Count request processed successfully: result={}", count);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Update socks", description = "Updates the details of a specific socks entry.")
    @ApiResponse(responseCode = "200", description = "Socks updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateSocks(@PathVariable Long id,
                                              @RequestBody @Valid SocksDto socksDto) {
        log.info("Processing update request: id={}, {}", id, socksDto);
        socksService.updateSocks(id, socksDto);
        log.info("Update request processed successfully for id={}", id);
        return ResponseEntity.ok("Socks updated successfully");
    }

    @Operation(summary = "Upload batch of socks", description = "Uploads socks from a CSV file.")
    @ApiResponse(responseCode = "200", description = "Batch uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or file")
    @PostMapping("/batch")
    public ResponseEntity<String> uploadBatch(@RequestParam("file") MultipartFile file) {
        try {
            socksService.uploadFile(file);
            log.info("Batch upload request processed successfully: fileName={}", file.getOriginalFilename());
            return ResponseEntity.ok("Batch upload successful");
        } catch (IllegalArgumentException e) {
            log.warn("Batch upload failed due to invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Batch upload failed due to an unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to upload batch: " + e.getMessage());
        }
    }
}

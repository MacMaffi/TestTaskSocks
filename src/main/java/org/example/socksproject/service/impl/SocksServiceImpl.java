package org.example.socksproject.service.impl;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.example.socksproject.dto.SocksDto;
import org.example.socksproject.model.Socks;
import org.example.socksproject.repository.SocksRepository;
import org.example.socksproject.service.SocksService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocksServiceImpl implements SocksService {

    private final SocksRepository socksRepository;

    @Override
    @Transactional
    public void incomeSocks(SocksDto socksDto) {
        log.info("Processing income for socks: {}", socksDto);
        try {
            Optional<Socks> existingSocks = socksRepository.findByColorAndCottonPercentage(
                    socksDto.getColor(), socksDto.getCottonPercentage());

            if (existingSocks.isPresent()) {
                Socks socks = existingSocks.get();
                log.info("Existing socks found: {}", socks);
                socks.setQuantity(socks.getQuantity() + socksDto.getQuantity());
                socksRepository.save(socks);
                log.info("Socks updated successfully: {}", socks);
            } else {
                Socks newSocks = new Socks(null, socksDto.getColor(), socksDto.getCottonPercentage(),
                        socksDto.getQuantity());
                socksRepository.save(newSocks);
                log.info("New socks added successfully: {}", newSocks);
            }
        } catch (OptimisticLockException e) {
            log.error("Optimistic lock conflict while processing income for socks: {}", socksDto, e);
            throw new IllegalStateException("Conflict detected: another transaction updated the data.", e);
        }
    }

    @Override
    @Transactional
    public void outcomeSocks(SocksDto socksDto) {
        log.info("Processing outcome for socks: {}", socksDto);
        try {
            Socks existingSocks = socksRepository.findByColorAndCottonPercentage(
                            socksDto.getColor(), socksDto.getCottonPercentage())
                    .orElseThrow(() -> {
                        log.warn("No socks found for color={} and cottonPercentage={}",
                                socksDto.getColor(), socksDto.getCottonPercentage());
                        return new IllegalArgumentException("No socks with parameters");
                    });

            log.info("Existing socks found for outcome: {}", existingSocks);

            if (socksDto.getQuantity() > existingSocks.getQuantity()) {
                log.warn("Not enough socks: requested={}, available={}",
                        socksDto.getQuantity(), existingSocks.getQuantity());
                throw new IllegalArgumentException("Not enough socks");
            }

            existingSocks.setQuantity(existingSocks.getQuantity() - socksDto.getQuantity());
            socksRepository.save(existingSocks);
            log.info("Socks updated successfully after outcome: {}", existingSocks);
        } catch (OptimisticLockException e) {
            log.error("Optimistic lock conflict while processing outcome for socks: {}", socksDto, e);
            throw new IllegalStateException("Conflict detected: another transaction updated the data.", e);
        }
    }

    @Override
    public int getSocksCount(String color, String comparison, int cottonPercentage) {
        log.info("Fetching socks count: color={}, comparison={}, cottonPercentage={}", color, comparison, cottonPercentage);

        int count = switch (comparison) {
            case "moreThan" -> socksRepository.countSocksByColorAndCottonPercentageGreaterThan(color, cottonPercentage);
            case "lessThan" -> socksRepository.countSocksByColorAndCottonPercentageLessThan(color, cottonPercentage);
            case "equal" -> socksRepository.countSocksByColorAndCottonPercentageEquals(color, cottonPercentage);
            default -> {
                log.warn("Invalid comparison operator: {}", comparison);
                throw new IllegalArgumentException("Invalid comparison");
            }
        };

        log.info("Socks count: {}", count);
        return count;
    }

    @Override
    @Transactional
    public void updateSocks(Long id, SocksDto socksDto) {
        Socks findSocks = socksRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Socks with id: " + id + " not found"));

        findSocks.setColor(socksDto.getColor());
        findSocks.setQuantity(socksDto.getQuantity());
        findSocks.setCottonPercentage(socksDto.getCottonPercentage());

        log.info("Socks updated successfully: {}", findSocks);
    }

    @Override
    public void uploadFile(MultipartFile file) {
        log.info("Uploading file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("File is empty: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("File is empty");
        }

        try (InputStream inputStream = file.getInputStream();
             Reader reader = new InputStreamReader(inputStream)) {

            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader("color", "cottonPercentage", "quantity")
                    .withSkipHeaderRecord()
                    .parse(reader);

            for (CSVRecord record : records) {
                log.debug("Processing record: {}", record);
                processRecord(record);
            }

            log.info("File processed successfully: {}", file.getOriginalFilename());

        } catch (IOException e) {
            log.error("Failed to process file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to process file", e);
        }
    }

    private void processRecord(CSVRecord record) {
        log.debug("Processing record: {}", record);

        String color = record.get("color");
        int cottonPercentage = Integer.parseInt(record.get("cottonPercentage"));
        int quantity = Integer.parseInt(record.get("quantity"));

        if (cottonPercentage < 0 || cottonPercentage > 100 || quantity < 1) {
            log.warn("Invalid data in CSV record: {}", record);
            throw new IllegalArgumentException("Invalid data in CSV file: " + record);
        }

        Optional<Socks> existingSocks = socksRepository.findByColorAndCottonPercentage(color, cottonPercentage);

        if (existingSocks.isPresent()) {
            Socks socks = existingSocks.get();
            socks.setQuantity(socks.getQuantity() + quantity);
            socksRepository.save(socks);
            log.debug("Updated socks: {}", socks);
        } else {
            Socks newSocks = new Socks(null, color, cottonPercentage, quantity);
            socksRepository.save(newSocks);
            log.debug("Added new socks: {}", newSocks);
        }
    }
}
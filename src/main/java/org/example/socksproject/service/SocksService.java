package org.example.socksproject.service;

import org.apache.commons.csv.CSVRecord;
import org.example.socksproject.dto.SocksDto;
import org.springframework.web.multipart.MultipartFile;

public interface SocksService {

    void incomeSocks(SocksDto socksDto);

    void outcomeSocks(SocksDto socksDto);

    int getSocksCount(String color, String comparison, int cottonPercentage);

    void updateSocks(Long id, SocksDto socksDto);

    void uploadFile(MultipartFile file);

}

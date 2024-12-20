package org.example.socksproject.helper;

import org.example.socksproject.dto.SocksDto;
import org.example.socksproject.model.Socks;

public class DtoGeneratorHelper {

    public static Socks generateSocks() {
        return Socks.builder()
                .id(null) // ID оставляем null для тестов
                .color("black")
                .cottonPercentage(50)
                .quantity(10)
                .version(0) // Оптимистическая блокировка
                .build();
    }

    public static SocksDto generateSocksDto() {
        return SocksDto.builder()
                .color("black")
                .cottonPercentage(50)
                .quantity(10)
                .build();
    }
}

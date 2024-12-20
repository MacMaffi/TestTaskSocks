package org.example.socksproject.repository;

import org.example.socksproject.model.Socks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocksRepository extends JpaRepository<Socks, Long> {

    Optional<Socks> findByColorAndCottonPercentage(String color, int cottonPercentage);

    int countSocksByColorAndCottonPercentageGreaterThan(String color, int cottonPercentage);

    int countSocksByColorAndCottonPercentageLessThan(String color, int cottonPercentage);

    int countSocksByColorAndCottonPercentageEquals(String color, int cottonPercentage);
}

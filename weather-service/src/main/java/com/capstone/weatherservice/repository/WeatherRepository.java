package com.capstone.weatherservice.repository;

import com.capstone.weatherservice.model.Weather;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {

    Optional<Weather> findFirstByCityAndStateCodeAndCountryCodeOrderByFetchedAtDesc(
            String city, String stateCode, String countryCode);

    @Query("SELECT w FROM Weather w WHERE " +
            "UPPER(w.city) = UPPER(:city) AND " +
            "UPPER(w.stateCode) = UPPER(:stateCode) AND " +
            "UPPER(w.countryCode) = UPPER(:countryCode) AND " +
            "w.expiresAt > :now " +
            "ORDER BY w.fetchedAt DESC")
    Optional<Weather> findFreshWeather(
            @Param("city") String city,
            @Param("stateCode") String stateCode,
            @Param("countryCode") String countryCode,
            @Param("now") Instant now);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select w from Weather w
        where w.city = :city and w.stateCode = :stateCode and w.countryCode = :countryCode
    """)
    Optional<Weather> findForUpdate(
            @Param("city") String city,
            @Param("stateCode") String stateCode,
            @Param("countryCode") String countryCode
    );

    Optional<Weather> findByCityAndStateCodeAndCountryCode(String city, String stateCode, String countryCode);
}
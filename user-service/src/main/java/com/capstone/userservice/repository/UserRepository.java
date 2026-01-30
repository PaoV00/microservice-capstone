package com.capstone.userservice.repository;

import com.capstone.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE :locationId MEMBER OF u.favoriteLocationIds")
    List<User> findUsersByFavoriteLocationId(@Param("locationId") String locationId);

    @Query("SELECT u FROM User u WHERE " +
            "UPPER(u.address.city) = UPPER(:city) AND " +
            "UPPER(u.address.stateCode) = UPPER(:stateCode) AND " +
            "UPPER(u.address.countryCode) = UPPER(:countryCode)")
    List<User> findUsersByLocation(
            @Param("city") String city,
            @Param("stateCode") String stateCode,
            @Param("countryCode") String countryCode);
}

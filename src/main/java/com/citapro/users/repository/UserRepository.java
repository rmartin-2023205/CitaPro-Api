package com.citapro.users.repository;

import com.citapro.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur WHERE LOWER(u.email) = LOWER(:email) AND u.active = true")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);
}


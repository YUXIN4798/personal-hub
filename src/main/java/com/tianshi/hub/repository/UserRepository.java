package com.tianshi.hub.repository;

import com.tianshi.hub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Modifying
    @Query("update User user set user.passwordHash = :passwordHash, user.updatedAt = CURRENT_TIMESTAMP where user.username = :username")
    void updatePasswordHash(@Param("username") String username, @Param("passwordHash") String passwordHash);
}

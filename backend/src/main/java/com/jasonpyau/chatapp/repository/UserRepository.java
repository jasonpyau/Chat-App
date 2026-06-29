package com.jasonpyau.chatapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jasonpyau.chatapp.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u from User u LEFT JOIN FETCH u.groupChats WHERE u.username = :username")
    Optional<User> findUserJoinedWithGroupChat(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

}

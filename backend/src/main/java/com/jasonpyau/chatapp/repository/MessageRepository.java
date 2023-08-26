package com.jasonpyau.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jasonpyau.chatapp.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
}

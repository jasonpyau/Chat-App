package com.jasonpyau.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jasonpyau.chatapp.entity.GroupChat;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    
}

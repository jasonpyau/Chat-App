package com.jasonpyau.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jasonpyau.chatapp.entity.GroupChat;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    
}

package com.jasonpyau.chatapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jasonpyau.chatapp.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(value = "SELECT * FROM message m WHERE m.group_chat = :groupId AND m.created_at < :createdBefore AND m.message_type != 'HIDDEN' ORDER BY m.created_at DESC", nativeQuery = true)
    public Page<Message> findAllInGroupChatWithPagination(Pageable pageable, @Param("groupId") Long groupId, @Param("createdBefore") Long before);
}

package com.jasonpyau.chatapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jasonpyau.chatapp.entity.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @Query(value = "SELECT * from attachment a WHERE a.id = :id AND a.group_chat = :groupChatId", nativeQuery = true)
    public Optional<Attachment> findByIdInGroupChat(@Param("id") Long id, @Param("groupChatId") Long groupChatId);
}
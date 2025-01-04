package com.jasonpyau.chatapp.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "message", indexes = {
    @Index(name = "created_at_ind", columnList = "created_at")
})
public class Message {

    public static final String INVALID_CONTENT = "'content' should be at most 1000 characters.";

    public enum MessageType {
        USER_JOIN, USER_LEAVE, USER_CHAT, USER_RENAME, HIDDEN
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "content", columnDefinition = "varchar(1000)")
    private String content;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "modified_at")
    private Long modifiedAt;

    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @JoinColumn(name = "sender")
    @ManyToOne(fetch = FetchType.EAGER)
    private User sender;

    @JoinColumn(name = "group_chat")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private GroupChat groupChat;

    @Column(name = "attachments")
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "message")
    private final Set<Attachment> attachments = new HashSet<>();

}

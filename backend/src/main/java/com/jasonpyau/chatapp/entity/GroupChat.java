package com.jasonpyau.chatapp.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Table(name = "group_chat")
public class GroupChat {
    
    public static final String INVALID_NAME = "'name' should be between 1-40 characters.";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Size(min = 1, max = 40, message = INVALID_NAME)
    @NotBlank(message = INVALID_NAME)
    private String name;

    @Column(name = "users")
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "group_chat_user",
                joinColumns = @JoinColumn(name = "group_chat_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @OrderBy("username ASC")
    private final Set<User> users = new HashSet<>();

    @Column(name = "messages")
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "groupChat")
    @JsonIgnore
    private final Set<Message> messages = new HashSet<>();

    @Column(name = "last_message_at")
    private Long lastMessageAt;

    public void addToGroupChat(User user) {
        user.getGroupChats().add(this);
        this.users.add(user);
    }

}

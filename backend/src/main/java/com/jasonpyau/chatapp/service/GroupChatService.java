package com.jasonpyau.chatapp.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jasonpyau.chatapp.entity.GroupChat;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.exception.InvalidInputException;
import com.jasonpyau.chatapp.form.NewGroupChatForm;
import com.jasonpyau.chatapp.repository.GroupChatRepository;
import com.jasonpyau.chatapp.util.CustomValidator;
import com.jasonpyau.chatapp.util.DateFormat;

@Service
public class GroupChatService {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupChatRepository groupChatRepository;

    private final CustomValidator<GroupChat> validator = new CustomValidator<>();
    
    public GroupChat newGroupChat(User user, NewGroupChatForm newGroupChatForm) {
        if (newGroupChatForm == null || newGroupChatForm.getUsernames() == null || newGroupChatForm.getUsernames().size() > 100) {
            throw new InvalidInputException("Invalid input");
        }
        GroupChat groupChat = GroupChat.builder()
                                    .name(newGroupChatForm.getName())
                                    .lastMessageAt(DateFormat.getUnixTime())
                                    .build();
        validator.validate(groupChat);
        groupChatRepository.save(groupChat);
        newGroupChatForm.getUsernames().add(user.getUsername());
        for (String username : newGroupChatForm.getUsernames()) {
            User member = userService.findUserJoinedWithGroupChat(username);
            groupChat.addToGroupChat(member);
        }
        return groupChatRepository.save(groupChat);
    }

    public Set<GroupChat> getGroupChats(User user) {
        return userService.findUserJoinedWithGroupChat(user.getUsername()).getGroupChats();
    }

    public Optional<GroupChat> findById(Long id) {
        return groupChatRepository.findById(id);
    }
}

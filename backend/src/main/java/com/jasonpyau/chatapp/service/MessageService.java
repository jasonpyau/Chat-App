package com.jasonpyau.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jasonpyau.chatapp.entity.GroupChat;
import com.jasonpyau.chatapp.entity.Message;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.entity.Message.MessageType;
import com.jasonpyau.chatapp.form.PaginationForm;
import com.jasonpyau.chatapp.repository.GroupChatRepository;
import com.jasonpyau.chatapp.repository.MessageRepository;
import com.jasonpyau.chatapp.util.CustomValidator;
import com.jasonpyau.chatapp.util.DateFormat;

@Service
public class MessageService {

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private GroupChatService groupChatService;

    private final CustomValidator<Message> validator = new CustomValidator<>();

    private final CustomValidator<PaginationForm> paginationFormValidator = new CustomValidator<>();
    
    public Message sendMessage(Long groupChatId, String messageContent, User user) {
        GroupChat groupChat = groupChatService.validateUserInGroupChat(user, groupChatId);
        Message message = Message.builder()
                                .content(messageContent)
                                .createdAt(DateFormat.getUnixTime())
                                .modifiedAt(DateFormat.getUnixTime())
                                .messageType(MessageType.USER_CHAT)
                                .sender(user)
                                .groupChat(groupChat)
                                .build();
        validator.validate(message);
        groupChat.setLastMessageAt(DateFormat.getUnixTime());
        groupChatRepository.save(groupChat);
        return messageRepository.save(message);
    }

    public Page<Message> getMessages(User user, Long groupId, PaginationForm paginationForm, Long before) {
        paginationFormValidator.validate(paginationForm);
        groupChatService.validateUserInGroupChat(user, groupId);
        Pageable pageable = PageRequest.of(paginationForm.getPageNum(), paginationForm.getPageSize());
        return messageRepository.findAllInGroupChatWithPagination(pageable, groupId, before);
    }

}

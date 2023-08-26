package com.jasonpyau.chatapp.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jasonpyau.chatapp.entity.Message;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.form.NewMessageForm;
import com.jasonpyau.chatapp.service.MessageService;
import com.jasonpyau.chatapp.service.UserService;

@Controller
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;
    
    @MessageMapping("/send/{id}")
    @SendTo("/topic/groupchat/{id}")
    public Message sendMessage(@DestinationVariable(value = "id") Long id, Principal principal, @Payload NewMessageForm newMessageForm) {
        User user = userService.getUserFromWebSocket(principal);
        return messageService.sendMessage(id, newMessageForm.getContent(), user);
    }
}

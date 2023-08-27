package com.jasonpyau.chatapp.controller;

import java.security.Principal;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jasonpyau.chatapp.annotation.GetUser;
import com.jasonpyau.chatapp.entity.Message;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.form.NewMessageForm;
import com.jasonpyau.chatapp.form.PaginationForm;
import com.jasonpyau.chatapp.service.MessageService;
import com.jasonpyau.chatapp.service.UserService;
import com.jasonpyau.chatapp.util.Response;

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

    @GetMapping(path = "/{id}/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> getMessages(@GetUser User user, 
                                                                @PathVariable("id") Long id, 
                                                                PaginationForm paginationForm, 
                                                                @RequestParam("before") Long before) {
        return Response.page(messageService.getMessages(user, id, paginationForm, before));
    }
}

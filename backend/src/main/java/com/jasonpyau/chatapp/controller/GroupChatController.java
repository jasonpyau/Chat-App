package com.jasonpyau.chatapp.controller;

import java.security.Principal;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jasonpyau.chatapp.annotation.GetUser;
import com.jasonpyau.chatapp.entity.Message;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.form.AddGroupChatUserForm;
import com.jasonpyau.chatapp.form.NewGroupChatForm;
import com.jasonpyau.chatapp.service.GroupChatService;
import com.jasonpyau.chatapp.service.UserService;
import com.jasonpyau.chatapp.util.Response;

@Controller
@RequestMapping("/api/groupchat")
public class GroupChatController {

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private UserService userService;

    @PostMapping(path = "/new", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> newGroupChat(@GetUser User user, @RequestBody NewGroupChatForm newGroupChatForm) {
        return new ResponseEntity<>(Response.createBody("groupChat", groupChatService.newGroupChat(user, newGroupChatForm)), HttpStatus.OK);
    }

    @GetMapping(path = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> getGroupChats(@GetUser User user) {
        return new ResponseEntity<>(Response.createBody("groupChats", groupChatService.getGroupChats(user)), HttpStatus.OK);
    }

    @MessageMapping("/update/{id}/users/add")
    @SendTo("/topic/groupchat/{id}")
    public Message addUser(@DestinationVariable(value = "id") Long id, Principal principal, @Payload AddGroupChatUserForm form) {
        User user = userService.getUserFromWebSocket(principal);
        return groupChatService.addUser(user, form, id);
    }

    @MessageMapping("/update/{id}/users/remove")
    @SendTo("/topic/groupchat/{id}")
    public Message removeUser(@DestinationVariable(value = "id") Long id, Principal principal) {
        User user = userService.getUserFromWebSocket(principal);
        return groupChatService.removeUser(user, id);
    }
    
}

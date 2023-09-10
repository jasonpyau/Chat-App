package com.jasonpyau.chatapp.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.jasonpyau.chatapp.entity.GroupChat;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.service.GroupChatService;
import com.jasonpyau.chatapp.service.RateLimitService;
import com.jasonpyau.chatapp.service.UserService;
import com.jasonpyau.chatapp.service.RateLimitService.Token;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupChatService groupChatService;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand cmd = accessor.getCommand();
        if (cmd == StompCommand.SUBSCRIBE) {
            Long id = null;
            String desination = accessor.getDestination();
            if (desination.startsWith("/topic/groupchat/")) {
                id = Long.valueOf(desination.split("/")[3]);
            }
            if (id != null) {
                User user = userService.getUserFromWebSocket(accessor.getUser());
                Optional<GroupChat> optional = groupChatService.findById(id);
                if (optional.isEmpty() || !optional.get().getUsers().contains(user)) {
                    return null;
                }
                RateLimitService.RateLimiter.rateLimit(user, Token.BIG_TOKEN);
            }

        }
        return message;
    }
}

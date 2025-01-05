package com.jasonpyau.chatapp.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.jasonpyau.chatapp.annotation.GetUser;
import com.jasonpyau.chatapp.annotation.RateLimitAPI;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.service.AttachmentService;
import com.jasonpyau.chatapp.service.RateLimitService.Token;


@Controller
@RequestMapping("/api/attachment")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;
    
    @GetMapping("/{groupChatId}/{messageId}/{attachmentId}")
    @RateLimitAPI(Token.BIG_TOKEN)  
    public ResponseEntity<byte[]> getAttachment(@GetUser User user, 
                                                @PathVariable("groupChatId") Long groupChatId, 
                                                @PathVariable("messageId") Long messageId, 
                                                @PathVariable("attachmentId") Long attachmentId) {
        byte[] bytes = attachmentService.getAttachmentBytes(user, groupChatId, messageId, attachmentId);
        return ResponseEntity.ok()
                            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)))
                            .contentType(MediaType.parseMediaType(attachmentService.getAttachmentType(attachmentId, groupChatId).getValue()))
                            .body(bytes);
    }
}

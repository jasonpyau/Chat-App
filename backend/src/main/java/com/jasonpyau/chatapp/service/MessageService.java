package com.jasonpyau.chatapp.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jasonpyau.chatapp.entity.Attachment;
import com.jasonpyau.chatapp.entity.GroupChat;
import com.jasonpyau.chatapp.entity.Message;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.entity.Message.MessageType;
import com.jasonpyau.chatapp.exception.InvalidInputException;
import com.jasonpyau.chatapp.exception.RateLimitException;
import com.jasonpyau.chatapp.form.NewMessageForm;
import com.jasonpyau.chatapp.form.PaginationForm;
import com.jasonpyau.chatapp.repository.MessageRepository;
import com.jasonpyau.chatapp.service.RateLimitService.Token;
import com.jasonpyau.chatapp.util.CustomValidator;
import com.jasonpyau.chatapp.util.DateFormat;

import io.github.bucket4j.ConsumptionProbe;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class MessageService {

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    @Lazy
    private GroupChatService groupChatService;

    private final CustomValidator<NewMessageForm> validator = new CustomValidator<>();

    private final CustomValidator<PaginationForm> paginationFormValidator = new CustomValidator<>();
    
    public Message sendMessage(Long groupChatId, NewMessageForm newMessageForm, User user) {
        validator.validate(newMessageForm);
        GroupChat groupChat = groupChatService.validateUserInGroupChat(user, groupChatId);
        Message message = Message.builder()
                                .content(StringUtils.hasText(newMessageForm.getContent()) ? newMessageForm.getContent() : "")
                                .createdAt(DateFormat.getUnixTime())
                                .modifiedAt(DateFormat.getUnixTime())
                                .messageType(MessageType.HIDDEN)
                                .sender(user)
                                .groupChat(groupChat)
                                .build();
        // Need to get the message id before adding any attachments.
        message = messageRepository.save(message);
        if (StringUtils.hasText(newMessageForm.getFile()) && StringUtils.hasText(newMessageForm.getFileName())) {
            try {
                // Costs extra (by a lot) to attach images to prevent spam.
                ConsumptionProbe consumptionProbe = RateLimitService.RateLimiter.rateLimit(user, Token.EXPENSIVE_TOKEN);
                if (!consumptionProbe.isConsumed()) {
                    throw new RateLimitException(TimeUnit.NANOSECONDS.toMillis(consumptionProbe.getNanosToWaitForRefill()));
                }
                Attachment attachment = attachmentService.newAttachment(newMessageForm.getFile(), newMessageForm.getFileName(), groupChat, message, user);
                // Attach the attachment back to socket listeners.
                message.getAttachments().add(attachment);
            } catch (InvalidInputException | RateLimitException | S3Exception e) {
                messageRepository.delete(message);
                // S3Exception does not have a handler, error output is shown in log.
                throw e; 
            }
        }
        // Attachments can take a long time, now you can finally show this message to users.
        message.setMessageType(MessageType.USER_CHAT);
        message.setCreatedAt(DateFormat.getUnixTime());
        message.setModifiedAt(DateFormat.getUnixTime());
        groupChat.setLastMessageAt(DateFormat.getUnixTime());
        groupChatService.save(groupChat);
        return messageRepository.save(message);
    }

    public Page<Message> getMessages(User user, Long groupId, PaginationForm paginationForm, Long before) {
        paginationFormValidator.validate(paginationForm);
        groupChatService.validateUserInGroupChat(user, groupId);
        Pageable pageable = PageRequest.of(paginationForm.getPageNum(), paginationForm.getPageSize());
        return messageRepository.findAllInGroupChatWithPagination(pageable, groupId, before);
    }

    public Message save(Message message) {
        return messageRepository.save(message);
    }

}

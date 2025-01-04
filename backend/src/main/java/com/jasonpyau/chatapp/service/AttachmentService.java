package com.jasonpyau.chatapp.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.hash.Hashing;
import com.jasonpyau.chatapp.entity.Attachment;
import com.jasonpyau.chatapp.entity.GroupChat;
import com.jasonpyau.chatapp.entity.Message;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.entity.Attachment.AttachmentType;
import com.jasonpyau.chatapp.exception.InvalidInputException;
import com.jasonpyau.chatapp.repository.AttachmentRepository;
import com.jasonpyau.chatapp.util.DateFormat;

import net.coobird.thumbnailator.Thumbnails;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class AttachmentService {
    
    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private AmazonS3Service amazonS3Service;
    
    @Autowired
    @Lazy
    private GroupChatService groupChatService;

    public Attachment newAttachment(String fileDataUrl, String fileName, GroupChat groupChat, Message message, User user) {
        try {
            String[] tokens = fileDataUrl.split(",");
            if (tokens.length <= 1) {
                throw new InvalidInputException(Attachment.INVALID_ATTACHMENT_TYPE);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(tokens[1]);
            byte[] compressedBytes = decodedBytes;
            if (decodedBytes.length > Attachment.SIZE_LIMIT_IN_MB*1024*1024) {
                throw new InvalidInputException(Attachment.ATTACHMENT_EXCEEDS_SIZE_LIMIT);
            }
            Tika tika = new Tika();
            String mediaType = tika.detect(decodedBytes);
            if (!Attachment.validAttachmentTypes().contains(mediaType)) {
                throw new InvalidInputException(Attachment.INVALID_ATTACHMENT_TYPE);
            }
            try {
                // Storage isn't free.
                if (decodedBytes.length > 8*1024*1024) {
                    // Let's aim for around 1.5 MB.
                    compressedBytes = compressImage(decodedBytes, (15*1024*1024)/10);
                } else if (decodedBytes.length > 6*1024*1024) {
                    // Let's aim for around 1.2 MB.
                    compressedBytes = compressImage(decodedBytes, (12*1024*1024)/10);
                } else if (decodedBytes.length > 4*1024*1024) {
                    // Let's aim for around 1 MB.
                    compressedBytes = compressImage(decodedBytes, (10*1024*1024)/10);
                } else if (decodedBytes.length > 2*1024*1024) {
                    // Let's aim for around 0.8 MB.
                    compressedBytes = compressImage(decodedBytes, (8*1024*1024)/10);
                } else if (decodedBytes.length > 1*1024*1024) {
                    // Let's aim for around 0.65 MB.
                    compressedBytes = compressImage(decodedBytes, (65*1024*1024)/100);
                } else if (decodedBytes.length > 1024*1024/4) {
                    compressedBytes = compressImage(decodedBytes, 0.8);
                }
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e.getMessage());
            }
            if (compressedBytes.length > decodedBytes.length) {
                // This picture was probably already compressed.
                compressedBytes = decodedBytes;
            }
            String fileHash = Hashing.sha512().hashBytes(decodedBytes).toString();
            AttachmentType attachmentType = AttachmentType.fromValue(mediaType);
            // Need to get the attachment id before actually building the attachment.
            Attachment attachment = attachmentRepository.save(new Attachment());
            attachment = Attachment.builder()
                                    .id(attachment.getId())
                                    .createdAt(DateFormat.getUnixTime())
                                    .attachmentType(attachmentType)
                                    .url(String.format("/api/attachment/%d/%d/%d", groupChat.getId(), message.getId(), attachment.getId()))
                                    .awsS3Key(String.format("attachment/%d/%s/%s", user.getId(), attachmentType.getValue(), fileHash))
                                    .fileName(fileName)
                                    .fileHash(fileHash)
                                    .fileByteSize(decodedBytes.length)
                                    .fileCompressByteSize(compressedBytes.length)
                                    .sender(user)
                                    .groupChat(groupChat)
                                    .message(message)
                                    .build();
            try {
                byte[] previous = amazonS3Service.getObject(attachment.getAwsS3Key());
                if (previous != null) {
                    attachment.setFileCompressByteSize(previous.length);
                } else {
                    amazonS3Service.putObject(compressedBytes, attachment.getAwsS3Key(), attachment.getAttachmentType().getValue());
                }
                return attachmentRepository.save(attachment);                      
            } catch (S3Exception s3Exception) {
                attachmentRepository.delete(attachment);
                throw s3Exception;
            }
        } catch (InvalidInputException | IllegalStateException e) {
            throw new InvalidInputException(e.getMessage());
        } catch (S3Exception e) {
            throw e;
        }
    }

    public byte[] getAttachmentBytes(User user, Long groupChatId, Long messageId, Long attachmentId) {
        groupChatService.validateUserInGroupChat(user, groupChatId);
        Optional<Attachment> optional = attachmentRepository.findByIdInGroupChat(attachmentId, groupChatId);
        if (!optional.isPresent()) {
            return null;
        }
        Attachment attachment = optional.get();
        return amazonS3Service.getObject(attachment.getAwsS3Key());
    }

    public AttachmentType getAttachmentType(Long attachmentId, Long groupChatId) {
        Optional<Attachment> optional = attachmentRepository.findByIdInGroupChat(attachmentId, groupChatId);
        if (!optional.isPresent()) {
            return null;
        }
        return optional.get().getAttachmentType();
    }

    private byte[] compressImage(byte[] bytes, int desiredBytes) throws IOException {
        return compressImage(bytes, (double)desiredBytes/bytes.length);
    }

    private byte[] compressImage(byte[] bytes, double quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(bytes))
            .scale(1.0)
            .outputQuality(quality)
            .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }
}

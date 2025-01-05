package com.jasonpyau.chatapp.entity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "attachment", indexes = {
    @Index(name = "created_at_ind", columnList = "created_at")
})
public class Attachment {

    public static final int SIZE_LIMIT_IN_MB = 10;
    public static final int FILE_NAME_LENGTH_LIMIT = 100;
    public static final String INVALID_ATTACHMENT_TYPE = "'attachmentType' should be one of the following: "+validAttachmentTypes().toString();
    public static final String ATTACHMENT_EXCEEDS_SIZE_LIMIT = "The attachment can be at most "+SIZE_LIMIT_IN_MB+"MB.";
    public static final String INVALID_FILE_NAME = "The attachment has too long of a file name. It can be at most "+FILE_NAME_LENGTH_LIMIT+" characters long";

    public enum AttachmentType {
        IMAGE_JPEG_VALUE(MediaType.IMAGE_JPEG_VALUE), IMAGE_PNG_VALUE(MediaType.IMAGE_PNG_VALUE), IMAGE_GIF_VALUE(MediaType.IMAGE_GIF_VALUE);

        @Getter
        @JsonValue
        private final String value;

        AttachmentType(String value) {
            this.value = value;
        }

        public static AttachmentType fromValue(String value) {
            switch (value) {
                case MediaType.IMAGE_JPEG_VALUE:
                    return IMAGE_JPEG_VALUE;
                case MediaType.IMAGE_PNG_VALUE:
                    return IMAGE_PNG_VALUE;
                case MediaType.IMAGE_GIF_VALUE:
                    return IMAGE_GIF_VALUE;
                default:
                    return null;
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "attachment_type")
    @Enumerated(EnumType.STRING)
    private AttachmentType attachmentType;

    // "/api/attachment/{groupChat.id}/{message.id}/{id}"
    @Column(name = "url")
    private String url;
    
    // If an attachment already exists with the same sender AND file type AND file hash, 
    // no need to create a new AWS S3 Object.
    // "attachment/{sender.id}/{attachmentType}/{fileHash}"
    @Column(name = "aws_S3_Key")
    @JsonIgnore
    private String awsS3Key;
    
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(name = "file_byte_size")
    private Integer fileByteSize;

    @Column(name = "file_compress_byte_size")
    private Integer fileCompressByteSize;

    @JoinColumn(name = "sender")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User sender;

    @JoinColumn(name = "group_chat")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private GroupChat groupChat;

    @JoinColumn(name = "message")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Message message;

    public static Set<String> validAttachmentTypes() {
        return List.of(AttachmentType.values()).stream().map(AttachmentType::getValue).collect(Collectors.toSet());
    }

}

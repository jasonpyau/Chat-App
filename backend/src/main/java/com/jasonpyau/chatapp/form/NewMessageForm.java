package com.jasonpyau.chatapp.form;

import org.springframework.util.StringUtils;

import com.jasonpyau.chatapp.entity.Attachment;
import com.jasonpyau.chatapp.entity.Message;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewMessageForm {

    public static final String MISSING_CONTENT_AND_ATTACHMENT = "If 'content' is blank, then 'file' and 'fileName' must not be blank.";
    
    @Size(max = 1000, message = Message.INVALID_CONTENT)
    private String content;

    private String file;

    @Size(max = Attachment.FILE_NAME_LENGTH_LIMIT, message = Attachment.INVALID_FILE_NAME)
    private String fileName;

    @AssertTrue(message = MISSING_CONTENT_AND_ATTACHMENT)
    public boolean hasContentOrFile() {
        return (StringUtils.hasText(content) || (StringUtils.hasText(file) && StringUtils.hasText(fileName)));
    }
}

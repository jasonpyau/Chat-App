package com.jasonpyau.chatapp.form;

import com.jasonpyau.chatapp.entity.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddGroupChatUserForm {
    
    @Size(min = 1, max = 30, message = User.DISPLAY_NAME_ERROR)
    @NotBlank(message = User.DISPLAY_NAME_ERROR)
    private String username;
}

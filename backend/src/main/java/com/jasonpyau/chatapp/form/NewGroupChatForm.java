package com.jasonpyau.chatapp.form;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewGroupChatForm {
    
    private String name;

    private Set<String> usernames;

}

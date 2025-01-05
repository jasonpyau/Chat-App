package com.jasonpyau.chatapp.form;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewGroupChatForm {
    
    private String name;

    private Set<String> usernames;

}

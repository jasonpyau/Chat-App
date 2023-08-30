package com.jasonpyau.chatapp.security.OAuth2UserInfo;

import java.util.Map;

public class DiscordOAuth2UserInfo extends OAuth2UserInfo {
    
    public DiscordOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String)attributes.get("id");
    }

    @Override
    public String getName() {
        return (String)attributes.get("global_name");
    }

    @Override
    public String getEmail() {
        return (String)attributes.get("email");
    }

    @Override
    public String getAvatarURL() {
        String avatar = (String)attributes.get("avatar");
        if (avatar == null) {
            return null;
        }
        return String.format("https://cdn.discordapp.com/avatars/%s/%s.png", getId(), avatar);
    }


}

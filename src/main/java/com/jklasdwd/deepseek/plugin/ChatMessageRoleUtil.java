package com.jklasdwd.deepseek.plugin;

import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;

public class ChatMessageRoleUtil {
    public static ChatMessageRole fromValue(String value) {
        for (ChatMessageRole role : ChatMessageRole.values()) {
            if (role.value().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown ChatMessageRole value: " + value);
    }
}
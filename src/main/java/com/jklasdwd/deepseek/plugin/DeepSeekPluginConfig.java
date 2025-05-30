package com.jklasdwd.deepseek.plugin;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import org.jetbrains.annotations.NotNull;


import java.util.HashMap;
import java.util.Map;

public class DeepSeekPluginConfig extends JavaAutoSavePluginConfig {
    public DeepSeekPluginConfig(@NotNull String saveName) {
        super(saveName);
    }
    public static final DeepSeekPluginConfig INSTANCE = new DeepSeekPluginConfig("DeepSeekPluginConfig");
    public final Value<Map<Long,Boolean>> chatgrouplist = typedValue(
            "chatgrouplist",
            createKType(
                    Map.class,
                    createKType(Long.class),
                    createKType(Boolean.class)
            ),
            new HashMap<Long, Boolean>(){}
    );
    public final Value<String> botbackground = typedValue(
            "botbackground",
            createKType(String.class),
            "You are a helpful ai assistant"
    );
    public final Value<String> apikey = typedValue(
            "apikey",
            createKType(String.class),
            ""
    );
    public final Value<String> model_id = typedValue(
            "model_id",
            createKType(String.class),
            ""
    );
    public final Value<Long> max_context_length = typedValue(
            "max_context_length",
            createKType(Long.class),
            100L
    );
    public final Value<Long> owner_id = typedValue(
            "owner_id",
            createKType(Long.class),
            1L
    );
    public final Value<String> web_search_background = typedValue(
            "web_search_background",
            createKType(String.class),
            ""
    );
    public final Value<String> web_search_prefix_url = typedValue(
            "web_search_prefix_url",
            createKType(String.class),
            ""
    );
}

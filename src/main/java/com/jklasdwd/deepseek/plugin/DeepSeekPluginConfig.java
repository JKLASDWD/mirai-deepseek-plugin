package com.jklasdwd.deepseek.plugin;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeepSeekPluginConfig extends JavaAutoSavePluginConfig {
    public DeepSeekPluginConfig(@NotNull String saveName) {
        super(saveName);
    }
    public static final DeepSeekPluginConfig INSTANCE = new DeepSeekPluginConfig("DeepSeekPluginConfig");
    public final Value<List<Long>> chatgrouplist = typedValue(
            "chatgrouplist",
            createKType(
                    List.class,
                    createKType(Long.class)
            ),
            new ArrayList<Long>() {}
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
}

package com.jklasdwd.deepseek.plugin;


import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeepSeekPluginWebSearchCommandChatData extends JavaAutoSavePluginData {
    DeepSeekPluginWebSearchCommandChatData(String savename) {
        super(savename);
    }
    public static final DeepSeekPluginWebSearchCommandChatData INSTANCE = new DeepSeekPluginWebSearchCommandChatData("DeepSeekPluginWebSearchCommandChatData");
    public final Value<Map<Long,List<Map<String,String>>>> web_context_data = typedValue(
            "web_context_data",
            createKType(
                    Map.class,
                    createKType(Long.class),
                    createKType(
                            List.class,
                            createKType(
                                    Map.class,
                                    createKType(String.class),
                                    createKType(String.class)
                            )
                    )
            ),
            new HashMap<>()
    );
}

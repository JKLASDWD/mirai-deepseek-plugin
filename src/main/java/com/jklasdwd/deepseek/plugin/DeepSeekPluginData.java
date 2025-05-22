package com.jklasdwd.deepseek.plugin;


import kotlin.Pair;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeepSeekPluginData extends JavaAutoSavePluginData {

    public DeepSeekPluginData(@NotNull String saveName) {
        super(saveName);
    }
    public static final DeepSeekPluginData INSTANCE = new DeepSeekPluginData("DeepSeekPluginData");
    public final Value<Map<Long,Map<Long,List<Pair<String,String>>>>> chatgroupcontext = typedValue(
            "chatgroupcontext",
            createKType(
                    Map.class,
                    createKType(Long.class),
                    createKType
                            (Map.class,
                                    createKType(Long.class),
                                    createKType(
                                            List.class,
                                            createKType(
                                                    Pair.class,
                                                    createKType(String.class),
                                                    createKType(String.class)
                                            )
                                    )
                            )
            ),
            new HashMap<Long,Map<Long,List<Pair<String,String>>>>(){}
    );
    public final Value<Map<Long,Long>> chatusercount = typedValue(
            "chatusercount",
            createKType(
                    Map.class,
                    createKType(Long.class),
                    createKType(Long.class)
                    ),
            new HashMap<Long,Long>(){}
    );

}

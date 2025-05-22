package com.jklasdwd.deepseek.plugin;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

public class DeepSeekPluginMain extends JavaPlugin {
    public static final DeepSeekPluginMain INSTANCE = new DeepSeekPluginMain();
    public static String ApiKey;
    public static String Model_Id;
    public static ArkService arkService;
    public static Long maxcontextlength;
    private DeepSeekPluginMain() {
        super(new JvmPluginDescriptionBuilder(
                "com.jklasdwd.deepseek.plugin",
                "0.1.0"
        ).build());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("DeepSeekPlugin enabled");
        this.reloadPluginConfig(DeepSeekPluginConfig.INSTANCE);
        this.reloadPluginData(DeepSeekPluginData.INSTANCE);

        ApiKey = DeepSeekPluginConfig.INSTANCE.apikey.get();
        Model_Id = DeepSeekPluginConfig.INSTANCE.model_id.get();
        arkService = ArkService.builder().apiKey(ApiKey).build();
        maxcontextlength = DeepSeekPluginConfig.INSTANCE.max_context_length.get();
    }
}

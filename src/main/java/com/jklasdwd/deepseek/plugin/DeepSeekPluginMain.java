package com.jklasdwd.deepseek.plugin;
import com.volcengine.ark.runtime.service.ArkService;
import net.mamoe.mirai.console.command.CommandManager;
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
        maxcontextlength = DeepSeekPluginConfig.INSTANCE.max_context_length.get();
        DeepSeekPluginPermission.UserPermission.getValue();
        DeepSeekPluginPermission.OwnerPermission.getValue();
        try{
            arkService = ArkService.builder().apiKey(ApiKey).build();
        }
        catch (Exception e){
            getLogger().error("ArkService连接失败！请检查Api密钥或模型ID是否正确", e);
            return;
        }
        CommandManager.INSTANCE.registerCommand(DeepSeekPluginUserCommand.INSTANCE,false);
        CommandManager.INSTANCE.registerCommand(DeepSeekPluginOwnerCommand.INSTANCE,false);
    }
    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("DeepSeekPlugin disabled");
        this.savePluginConfig(DeepSeekPluginConfig.INSTANCE);
        this.savePluginData(DeepSeekPluginData.INSTANCE);
        CommandManager.INSTANCE.unregisterCommand(DeepSeekPluginUserCommand.INSTANCE);
        CommandManager.INSTANCE.unregisterCommand(DeepSeekPluginOwnerCommand.INSTANCE);
        try{
            arkService.shutdownExecutor();
        }
        catch (Exception e){
            getLogger().info("arKService 关闭失败！");
        }
    }
}

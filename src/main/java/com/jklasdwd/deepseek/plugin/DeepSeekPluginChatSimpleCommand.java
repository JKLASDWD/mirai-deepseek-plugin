package com.jklasdwd.deepseek.plugin;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import jdk.jfr.Description;
import kotlin.Pair;
import net.mamoe.mirai.console.command.CommandOwner;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepSeekPluginChatSimpleCommand extends JSimpleCommand {
    public Permission userpermission = PermissionService.getInstance().get(PermissionId.parseFromString("com.jklasdwd.deepseek.plugin:user-permission"));
    public static final DeepSeekPluginChatSimpleCommand INSTANCE = new DeepSeekPluginChatSimpleCommand();
    public DeepSeekPluginChatSimpleCommand() {
        super(DeepSeekPluginMain.INSTANCE,"chat");
        setPermission(userpermission);
        setDescription("对话指令");
    }
    private static final Value<Map<Long, Map<Long, List<Pair<String, String>>>>> plugin_data_group_user_context = DeepSeekPluginData.INSTANCE.chatgroupcontext;
    private static final Value<Map<Long, Boolean>> plugin_config_group_map = DeepSeekPluginConfig.INSTANCE.chatgrouplist;
    @Handler
    public void onCommand(MemberCommandSender sender, @Name("文本")String chattext) {
        if(DeepSeekPluginMain.ApiKey.isEmpty() || DeepSeekPluginMain.Model_Id.isEmpty()) {
            sender.sendMessage("ApiKey或Model_ID配置有误！");
            return;
        }

        Long groupId = sender.getGroup().getId();
        Long userId = sender.getUser().getId();
        Map<Long,Boolean> group_map = plugin_config_group_map.get();
        if(!group_map.containsKey(groupId)) {
            sender.sendMessage("群权限未开通！");
            return;
        }
        Map<Long, Map<Long, List<Pair<String, String>>>> group_user_context = plugin_data_group_user_context.get();
        Map<Long, List<Pair<String, String>>> user_context_map = group_user_context.get(groupId);

        List<Pair<String, String>> context = user_context_map.get(userId);
        if (context == null ) {
            // 创建上下文List
            if(!group_user_context.containsKey(sender.getGroup().getId())) {
                group_user_context.put(sender.getGroup().getId(), new HashMap<>());
            }
            context = new ArrayList<>();
            context.add(new Pair<>(
                    "system",
                    DeepSeekPluginConfig.INSTANCE.botbackground.get()
            ));
        }
        // 大于设定最长上下文，清空
        else if (context.size() > DeepSeekPluginMain.maxcontextlength) {
            sender.sendMessage("达到上下文限制！已清空上下文");
            context.clear();
            context.add(new Pair<>(
                    "system",
                    DeepSeekPluginConfig.INSTANCE.botbackground.get()
            ));
        }
        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        // 遍历列表添加上下文

        for (Pair<String, String> msg : context) {
            final ChatMessage mes = ChatMessage.builder()
                    .role(ChatMessageRoleUtil.fromValue(msg.getFirst()))
                    .content(msg.getSecond())
                    .build();
            messages.add(mes);
        }
        // 添加用户的请求、
        final ChatMessage mes = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(chattext)
                .build();
        messages.add(mes);


        try {
            // 发起请求
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(DeepSeekPluginMain.Model_Id)
                    .messages(messages)
                    .build();
            String response_text = DeepSeekPluginMain
                    .arkService
                    .createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent()
                    .toString();
            final ChatMessage response = ChatMessage.builder()
                    .role(ChatMessageRole.ASSISTANT)
                    .content(response_text)
                    .build();
            messages.add(response);

            context.add(new Pair<>(
                    "user",
                    chattext
            ));
            context.add(new Pair<>(
                    "assistant",
                    response_text
            ));

            //发送消息
            sender.sendMessage(response_text);
        }
        catch (Exception e) {
            sender.sendMessage("请求出错！");
            DeepSeekPluginMain.INSTANCE.getLogger().info(e.getMessage());
        }

        // 更新
        user_context_map.put(userId,context);
        group_user_context.put(groupId,user_context_map);
        plugin_data_group_user_context.set(group_user_context);
    }
}

package com.jklasdwd.deepseek.plugin;
import com.volcengine.ark.runtime.exception.ArkHttpException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import kotlin.Pair;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepSeekPluginUserCommand extends JCompositeCommand {
    public static final DeepSeekPluginUserCommand INSTANCE = new DeepSeekPluginUserCommand();
    public Permission userpermission = PermissionService.getInstance().get(PermissionId.parseFromString("com.jklasdwd.deepseek.plugin:userpermission"));
    public DeepSeekPluginUserCommand() {
        super(DeepSeekPluginMain.INSTANCE,"chat");
        setDescription("基本用户指令");
        setPermission(userpermission);
        this.getUsage();
    }
    public static Value<Map<Long, Map<Long, List<Pair<String, String>>>>> plugin_data_group_user_context = DeepSeekPluginData.INSTANCE.chatgroupcontext;
    public void onCommand(MemberCommandSender sender, String chattext) {
        if(DeepSeekPluginMain.ApiKey.isEmpty() || DeepSeekPluginMain.Model_Id.isEmpty()) {
            sender.sendMessage("ApiKey或Model_ID配置有误！");
            return;
        }
        Long groupId = sender.getGroup().getId();
        Long userId = sender.getUser().getId();

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
    @SubCommand("clear")
    @Description("清除上下文")
    public void clear_user_context(MemberCommandSender sender) {
        Long groupId = sender.getGroup().getId();
        Long userId = sender.getUser().getId();

        Map<Long, Map<Long, List<Pair<String, String>>>> group_user_context = plugin_data_group_user_context.get();
        Map<Long,List<Pair<String,String>>> user_context_map = group_user_context.get(groupId);
        if(user_context_map == null || !user_context_map.containsKey(userId)) {
                sender.sendMessage("上下文为空！");
                return;
        }
        user_context_map.remove(userId);
        sender.sendMessage("清除成功！");
        group_user_context.put(groupId,user_context_map);
        plugin_data_group_user_context.set(group_user_context);

    }
    @SubCommand("check")
    @Description("检查并输出上下文")
    public void check_user_context(MemberCommandSender sender) {
        Long groupId = sender.getGroup().getId();
        Long userId = sender.getUser().getId();
        Bot bot = sender.getBot();
        Map<Long, Map<Long, List<Pair<String, String>>>> group_user_context = plugin_data_group_user_context.get();
        Map<Long,List<Pair<String,String>>> user_context_map = group_user_context.get(groupId);
        if(user_context_map == null || !user_context_map.containsKey(userId)) {
            sender.sendMessage("上下文为空！");
            return;
        }
        ForwardMessageBuilder m = new ForwardMessageBuilder(sender.getGroup());
        for (Pair<String, String> msg : user_context_map.get(userId)) {
            m.add(bot,new MessageChainBuilder()
                    .append("消息类型：\n")
                    .append(msg.getFirst())
                    .append("消息内容：\n")
                    .append(msg.getSecond())
                    .build()
            );
        }
        sender.sendMessage(m.build());
    }
}

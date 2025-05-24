package com.jklasdwd.deepseek.plugin;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import kotlin.Pair;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepSeekPluginUserCommand extends JCompositeCommand {

    public Permission userpermission = PermissionService.getInstance().get(PermissionId.parseFromString("com.jklasdwd.deepseek.plugin:user-permission"));
    public DeepSeekPluginUserCommand() {
        super(DeepSeekPluginMain.INSTANCE,"chatuser");
        setDescription("基本用户指令");
        setPermission(userpermission);
        this.getUsage();
    }
    public static final DeepSeekPluginUserCommand INSTANCE = new DeepSeekPluginUserCommand();
    private static final Value<Map<Long, Map<Long, List<Pair<String, String>>>>> plugin_data_group_user_context = DeepSeekPluginData.INSTANCE.chatgroupcontext;
    private static final Value<Map<Long,Long>> plugin_data_chat_user_count = DeepSeekPluginData.INSTANCE.chatusercount;
    @SubCommand("clear")
    @Description("清除上下文")
    public void clear_user_context(MemberCommandSender sender) {
        Long groupId = sender.getGroup().getId();
        Long userId = sender.getUser().getId();

        Map<Long, Map<Long, List<Pair<String, String>>>> group_user_context = plugin_data_group_user_context.get();
        Map<Long,Long> user_count = plugin_data_chat_user_count.get();
        Map<Long,List<Pair<String,String>>> user_context_map = group_user_context.get(groupId);
        if(user_context_map == null || !user_context_map.containsKey(userId)) {
                sender.sendMessage("上下文为空！");
                return;
        }
        user_context_map.remove(userId);
        sender.sendMessage(sender.getName()+user_count.get(userId)+"条记录清除成功！");
        group_user_context.put(groupId,user_context_map);
        user_count.put(userId,0L);
        plugin_data_group_user_context.set(group_user_context);
        plugin_data_chat_user_count.set(user_count);
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

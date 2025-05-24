package com.jklasdwd.deepseek.plugin;

import kotlin.Pair;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.UserCommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.List;
import java.util.Map;

public class DeepSeekPluginOwnerCommand extends JCompositeCommand {
    public static final DeepSeekPluginOwnerCommand INSTANCE = new DeepSeekPluginOwnerCommand();
    public Permission permission = PermissionService.getInstance().get(PermissionId.parseFromString("com.jklasdwd.deepseek.plugin:owner-permission"));
    public DeepSeekPluginOwnerCommand() {
        super(DeepSeekPluginMain.INSTANCE,"chatowner");
        setDescription("chat相关参数指令");
        setPermission(permission);
        this.getUsage();
    }
    private static final Value<Map<Long, Map<Long, List<Pair<String,String>>>>> plugin_data_user_context = DeepSeekPluginData.INSTANCE.chatgroupcontext;
    @SubCommand("clear")
    @Description("清除特定群聊内特定用户的上下文")
    public void clear_user_context(CommandSender sender,@Name("群号")Long group,@Name("QQ号")Long user_id) {
        Map<Long, Map<Long, List<Pair<String,String>>>> context = plugin_data_user_context.get();
        Map<Long, List<Pair<String,String>>> group_context = context.get(group);
        if(group_context == null) {
            sender.sendMessage("该群没有上下文记录！");
            return;
        }
        List<Pair<String,String>> user_context_list = group_context.get(user_id);
        if(user_context_list == null) {
            sender.sendMessage("用户上下文为空！");
            return;
        }
        sender.sendMessage("清除成功！，删除"+user_context_list.size()+"条记录");
        user_context_list.clear();
        group_context.put(user_id, user_context_list);
        context.put(group, group_context);
        plugin_data_user_context.set(context);
    }
    @SubCommand("check")
    @Description("输出特定群内特定用户的上下文")
    public void check_user_context(UserCommandSender sender, @Name("群号")Long group, @Name("QQ号")Long user_id) {
        Map<Long, Map<Long, List<Pair<String, String>>>> context = plugin_data_user_context.get();
        Map<Long, List<Pair<String, String>>> group_context = context.get(group);
        if (group_context == null) {
            sender.sendMessage("该群没有上下文记录！");
            return;
        }
        List<Pair<String, String>> user_context_list = group_context.get(user_id);
        if (user_context_list == null) {
            sender.sendMessage("用户上下文为空！");
            return;
        }
        ForwardMessageBuilder mes = new ForwardMessageBuilder(sender.getUser());
        for (Pair<String, String> user_context : user_context_list) {
            mes.add(sender.getBot(), new MessageChainBuilder()
                    .append("消息类型：\n")
                    .append(user_context.getFirst())
                    .append("消息内容：\n")
                    .append(user_context.getSecond())
                    .build()
            );
        }
        sender.sendMessage(mes.build());
    }
    public void perm_group_permission(UserCommandSender sender,@Name("群号")Long group){
        CommandManager.INSTANCE.executeCommand(sender,
                new MessageChainBuilder()
                        .append("/perm ")
                        .append("m")
                        .append(group.toString())
                        .append(".* com.jklasdwd.deepseek.plugin:userpermission")
                        .build(),
                true
        );
        sender.sendMessage("群权限赋予成功！");
    }
}

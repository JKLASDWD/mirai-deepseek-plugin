package com.jklasdwd.deepseek.plugin;

import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.UserCommandSender;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DeepSeekPluginWebSimpleCommand extends JSimpleCommand {
    DeepSeekPluginChatSimpleCommand INSTANCE = new DeepSeekPluginChatSimpleCommand();
    Permission userpermission = PermissionService.getInstance().get(PermissionId.parseFromString("user-permission"));
    public DeepSeekPluginWebSimpleCommand() {
        super(DeepSeekPluginMain.INSTANCE,"web");
        setPermission(userpermission);
        setDescription("网页省流总结");
    }
    @Handler
    public void OnCommand(MemberCommandSender sender, String url){

        Value<String> search_prompt = DeepSeekPluginConfig.INSTANCE.web_search_background;
        Value<String> prefix_url = DeepSeekPluginConfig.INSTANCE.web_search_prefix_url;
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix_url.get()+url))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;

        try{
            response = client.send(request,HttpResponse.BodyHandlers.ofString());

        }
        catch (IOException | InterruptedException e){
            sender.sendMessage("请求出错！");
            return;
        }

    }
}

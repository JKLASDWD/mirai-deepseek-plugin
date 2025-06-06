package com.jklasdwd.deepseek.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volcengine.ark.runtime.model.completion.chat.*;

import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionService;

import java.io.IOException;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DeepSeekPluginWebSimpleCommand extends JSimpleCommand {
    public static final DeepSeekPluginWebSimpleCommand INSTANCE = new DeepSeekPluginWebSimpleCommand();
    Permission userpermission = PermissionService.getInstance().get(PermissionId.parseFromString("com.jklasdwd.deepseek.plugin:user-permission"));

    public DeepSeekPluginWebSimpleCommand() {
        super(DeepSeekPluginMain.INSTANCE, "web");
        setPermission(userpermission);
        setDescription("网页省流总结");
    }

    private final Value<String> search_prompt = DeepSeekPluginConfig.INSTANCE.web_search_background;
    private final Value<Map<Long,List<Map<String,String>>>> plugin_data_group_websearch_context_map = DeepSeekPluginWebSearchCommandChatData.INSTANCE.web_context_data;
    @Handler
    public void OnCommand(MemberCommandSender sender, @Name("url|文本")String text) {
        if (DeepSeekPluginMain.ApiKey.isEmpty() || DeepSeekPluginMain.Model_Id.isEmpty()) {
            sender.sendMessage("ApiKey或Model_ID配置有误！");
            return;
        }

        Map<Long,List<Map<String,String>>> group_websearch_context_map = plugin_data_group_websearch_context_map.get();
        List<Map<String,String>> context_list = group_websearch_context_map.get(sender.getGroup().getId());
        Map<String, Object> urlSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "url", Map.of("type", "string", "description", "要抓取的网页链接")
                ),
                "required", List.of("url")
        );
        List<ChatMessage> messages = new ArrayList<>();
        if(context_list!=null && context_list.size() >= DeepSeekPluginConfig.INSTANCE.max_context_length.get()) {
            sender.sendMessage("上下文已满，清除后重新开始对话。");
            context_list.clear();
        }
        if(context_list == null || context_list.isEmpty()){
            context_list = new ArrayList<>();
            context_list.add(
                    Map.of(
                            "system",
                            search_prompt.get()
            ));
        }
        context_list.add(
                Map.of(
                        "user",
                        text
                )
        );


        context_list.forEach(map ->{
            if(map.containsKey("tool_call")){
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(map.get("tool_call"));
                    ChatToolCall toolCall = mapper.convertValue(node, ChatToolCall.class);
                    messages.add(new ChatMessage.Builder()
                            .role(ChatMessageRole.ASSISTANT)
                            .content("")
                            .toolCalls(Collections.singletonList(toolCall))
                            .build());
                } catch (JsonProcessingException e) {
                    DeepSeekPluginMain.INSTANCE.getLogger().error("工具调用参数解析错误", e);
                }
            }
            else if(map.containsKey("tool")){
                messages.add(new ChatMessage.Builder()
                        .role(ChatMessageRole.TOOL)
                        .toolCallId(map.get("tool_id"))
                        .content(map.get("tool"))
                        .build());
            }
            else {
                messages.add(new ChatMessage.Builder()
                        .role(ChatMessageRoleUtil.fromValue(map.keySet().iterator().next()))
                        .content(map.get(map.keySet().iterator().next()))
                        .build());
            }
        }
        );
        List<ChatTool> tools = Collections.singletonList(
                new ChatTool(
                        "function",
                        new ChatFunction.Builder()
                                .name("get_url_content")
                                .description("获取网页内容")
                                .parameters(urlSchema)
                                .build()
                )
        );


        while (true){
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(DeepSeekPluginMain.Model_Id)
                    .messages(messages)
                    .tools(tools)
                    .build();
        try {
            ChatMessage response_message = DeepSeekPluginMain.arkService
                    .createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage();
            // tool_calls 调用
            if (response_message.getToolCalls() != null && !response_message.getToolCalls().isEmpty()) {
                for (ChatToolCall toolCall : response_message.getToolCalls()) {
                    context_list.add(Map.of(
                            "tool_call",
                            new ObjectMapper().writeValueAsString(toolCall) // 将工具调用转换为字符串存储;
                    ));
                    messages.add(response_message);
                    if ("get_url_content".equals(toolCall.getFunction().getName())) {
                        String functionArgs = toolCall.getFunction().getArguments();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node;
                        try {
                            node = mapper.readTree(functionArgs);
                        }
                        catch (JsonProcessingException e){
//                            sender.sendMessage("获取网页内容失败，参数解析错误：" + e.getMessage());
                            context_list.add(Map.of(
                                    "assistant",
                                    "获取网页内容失败，参数解析错误：" + e.getMessage()
                            ));
                            messages.add(new ChatMessage.Builder()
                                    .role(ChatMessageRole.ASSISTANT)
                                    .content("获取网页内容失败，参数解析错误：" + e.getMessage())
                                    .build());
                            return;
                        }
                        String url = node.get("url").asText();
                        String content = get_url_content(url);
                        context_list.add(
                                Map.of(
                                        "tool_id",
                                        toolCall.getId(),
                                        "tool",
                                        content
                                )
                        );
                        messages.add(new ChatMessage.Builder()
                                .role(ChatMessageRole.TOOL)
                                .content(content)
                                .toolCallId(toolCall.getId())
                                .build());
                    }
                }
            }
            // 普通回复
            else {
                String response_text = response_message.getContent().toString();
                context_list.add(Map.of(
                        "assistant",
                        response_text
                ));
                messages.add(response_message);
                group_websearch_context_map.put(sender.getGroup().getId(), context_list);
                plugin_data_group_websearch_context_map.set(group_websearch_context_map);
                sender.sendMessage(response_text);
                break;
            }
        } catch (Exception e) {
            DeepSeekPluginMain.INSTANCE.getLogger().error(e.getMessage());
            DeepSeekPluginMain.INSTANCE.getLogger().error("请求出错！");
            sender.sendMessage("请求出错!");
            break;
        }
        }
    }

    public static String get_url_content(String url) {
        if (url == null || url.isEmpty()) {
            return "URL不能为空。";
        }

        if (!url.startsWith("http")) {
            url = "https://" + url;
        }
        if (DeepSeekPluginConfig.INSTANCE.web_search_prefix_url.get() != null && !DeepSeekPluginConfig.INSTANCE.web_search_prefix_url.get().isEmpty()) {
            url = DeepSeekPluginConfig.INSTANCE.web_search_prefix_url.get() + url;
        }
        try {
            Document doc = Jsoup.connect(url).get();
            doc.select("script, style, link, noscript, iframe, header, footer, nav").remove();


            StringBuilder sb = new StringBuilder();

            // 提取标题
            for (Element heading : doc.select("h1, h2, h3")) {
                sb.append("\n## ").append(heading.text()).append("\n");
            }

            // 提取段落
            for (Element p : doc.select("p")) {
                sb.append(p.text()).append("\n");
            }

            // 提取条列
            for (Element li : doc.select("ul li, ol li")) {
                sb.append("• ").append(li.text()).append("\n");
            }

            // 提取表格
            for (Element tr : doc.select("table tr")) {
                for (Element td : tr.select("th, td")) {
                    sb.append(td.text()).append(" | ");
                }
                sb.append("\n");
            }
            for (Element a: doc.select("a[href]")) {
                String linkText = a.text();
                String linkHref = a.attr("href");
                sb.append("\n[链接: ").append(linkText).append("](").append(linkHref).append(")\n");
            }
            return sb.toString().trim();

        } catch (IOException e) {
            return "无法获取网页内容，请检查URL是否正确或网络连接是否正常。";
        }
    }

}

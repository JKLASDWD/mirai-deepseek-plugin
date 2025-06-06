

# mirai-deepseek-plugin

一个简单的对接字节火山引擎实现LLM对话的插件

施工中...

- [x] UserCommand
- [x] Permission
- [x] OwnerCommand
- Data
- [x] 群组上下文存储
- [x] 群组多个用户上下文隔离对话
- [ ] 统计每日使用量
- [x] 网页省流操作
- [ ] 网页省流缓存数据清除指令
- [ ] 群组同一上下文语境对话



## 指令列表

```
/chat <文本> #对话
/chatuser clear #清除上下文
/chatuser check #检查上下文并，以转发记录输出
/chatowner clear <群号> <用户ID> #清除群聊内用户上下文
/chatowner check <群号> <用户ID> #检查群聊内用户上下文，以转发聊天记录输出
/chatowner perm <群号> #开启某个群聊的对话权限
/web <文本> # Function_call实现，智能识别url进行网页省流操作
/
```

## 权限

```
com.jklasdwd.deepseek.plugin:user-permission #用户权限
com.jklasdwd.deepseek.plugin:owner-permission #所有者权限
```

使用时请自行使用perm赋予所有者权限



## 配置

```yaml
chatgrouplist: {}

botbackground: You are a helpful ai assistant
apikey: ''
model_id: ''
max_context_length: 100
owner_id: 1
web_search_background: ''
web_search_prefix_url: ''
```

apikey和model_id的填写请参阅火山引擎[官方文档](https://www.volcengine.com/docs/82379/)




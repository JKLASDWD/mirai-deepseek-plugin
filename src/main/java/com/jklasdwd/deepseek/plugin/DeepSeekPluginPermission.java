package com.jklasdwd.deepseek.plugin;

import kotlin.Lazy;
import kotlin.LazyKt;
import net.mamoe.mirai.console.permission.*;
import net.mamoe.mirai.contact.User;

public class DeepSeekPluginPermission {
    public static final Lazy<Permission> OwnerPermission = LazyKt.lazy(()->{
        try{
            return PermissionService.getInstance().register(
                DeepSeekPluginMain.INSTANCE.permissionId("owner-permission"),
                    "DeepSeekPlugin拥有者权限",
                DeepSeekPluginMain.INSTANCE.getParentPermission()
            );
        }
        catch (PermissionRegistryConflictException e){
            throw new RuntimeException(e);
        }
    });
    public static final Lazy<Permission> UserPermission = LazyKt.lazy(
            ()->{
                try{
                    return PermissionService.getInstance().register(
                            DeepSeekPluginMain.INSTANCE.permissionId("user-permission"),
                            "DeepSeekPlugin用户权限",
                            OwnerPermission.getValue()
                    );
                }
                catch (PermissionRegistryConflictException e){
                    throw new RuntimeException(e);
                }
            }
    );

//    public static boolean hasOwnerPermission(User user) {
//        PermitteeId pid;
//
//    }
}

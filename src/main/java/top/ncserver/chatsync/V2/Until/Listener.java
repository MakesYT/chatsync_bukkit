package top.ncserver.chatsync.V2.Until;

import com.alibaba.fastjson.JSONObject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.ncserver.chatsync.Chatsync;
import top.ncserver.chatsync.Client;
import top.ncserver.chatsync.V2.Until.hook.HookManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Listener implements org.bukkit.event.Listener {
    private static final String channel = "chatimg:img";
    Map<String, Object> msg = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().hasPermission("chatsync.invisibleMsg")) {
            return;
        }
        if (Chatsync.UnconditionalAutoSync) {
            if (!HookManager.hooks.isEmpty())
                if (HookManager.check(event.getMessage()))
                    return;
            msg.clear();
            msg.put("type", "msg");
            msg.put("sender", event.getPlayer().getDisplayName());
            msg.put("msg", event.getMessage());
            JSONObject jo = new JSONObject(msg);
            MsgTool.msgSend(Client.session, jo.toJSONString());
        } else if (event.getMessage().startsWith(Chatsync.AutoSyncPrefix)) {
            msg.clear();
            msg.put("type", "msg");
            msg.put("sender", event.getPlayer().getDisplayName());
            msg.put("msg", event.getMessage().replaceFirst(Chatsync.AutoSyncPrefix, ""));
            JSONObject jo = new JSONObject(msg);
            MsgTool.msgSend(Client.session, jo.toJSONString());
        }


    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        if (event.getEntity().hasPermission("chatsync.invisible")) {
            return;
        }
        msg.clear();
        msg.put("type", "playerJoinAndQuit");
        msg.put("player", event.getPlayer().getDisplayName());
        msg.put("msg", "加入了服务器");
        JSONObject jo = new JSONObject(msg);
        MsgTool.msgSend(Client.session, jo.toJSONString());

        Player player = event.getPlayer();
        try {
            Class<? extends CommandSender> senderClass = player.getClass();
            Method addChannel = senderClass.getDeclaredMethod("addChannel", String.class);
            addChannel.setAccessible(true);
            addChannel.invoke(player, channel);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().hasPermission("chatsync.invisible")) {
            return;
        }
        msg.clear();
        msg.put("type", "playerJoinAndQuit");
        msg.put("player", event.getPlayer().getDisplayName());
        msg.put("msg", "退出了服务器");
        JSONObject jo = new JSONObject(msg);
        MsgTool.msgSend(Client.session, jo.toJSONString());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().hasPermission("chatsync.invisible")) {
            return;
        }
        msg.clear();
        msg.put("type", "playerDeath");
        msg.put("player", event.getEntity().getDisplayName());
        msg.put("msg", event.getDeathMessage());
        JSONObject jo = new JSONObject(msg);
        MsgTool.msgSend(Client.session, jo.toJSONString());
    }
}

package top.ncserver.chatsync.V2.Until.hook;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.maxgamer.quickshop.api.event.QSHandleChatEvent;
import top.ncserver.chatsync.Chatsync;

public class QSHook implements Hook, Listener {
    public QSHook() {
        Plugin quickShop = Bukkit.getPluginManager().getPlugin("QuickShop");
        if (quickShop != null) {
            HookManager.hooks.add(this);
            Bukkit.getPluginManager().registerEvents(this, Chatsync.getPlugin(Chatsync.class));
            return;
        }
        Plugin quickShopA = Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        if (quickShopA != null) {
            HookManager.hooks.add(this);
            Bukkit.getPluginManager().registerEvents(this, Chatsync.getPlugin(Chatsync.class));
        }
    }

    @Override
    public void hook() {

    }

    private String lastMsg = "";

    @Override
    public String getLastMsg() {
        return lastMsg;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQSChat(QSHandleChatEvent e) {
        lastMsg = e.getMessage();
    }
}

package top.ncserver.chatsync;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import top.ncserver.chatsync.Until.Metrics;
import top.ncserver.chatsync.V2.Until.Img;
import top.ncserver.chatsync.V2.Until.ImgTools;
import top.ncserver.chatsync.V2.Until.MsgTool;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Chatsync extends JavaPlugin {
    public Logger logger = this.getLogger();
    private static final int IDX = 6969;
    Client c;
    Map<String, Object> msg = new HashMap<>();
    public static YamlConfiguration config;

    private static final String channel = "chatimg:img";
    public static boolean isOnDisable = false;
    ///接收来自客户端的图片,但由于同步原因,使用uuid作为唯一键,防止冲突
    public static Map<String, Img> imgMap = new LinkedHashMap<String, Img>();

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, channel,
                (channel, player, message) -> {
                    String msg = read(message);
                    msg = msg.substring(msg.indexOf("{"));
                    if (msg.contains("base64imgdata")) {
                        try {
                            JsonObject jsonObject = new JsonParser().parse(msg).getAsJsonObject();
                            String imgID = jsonObject.get("id").getAsString();
                            if (imgMap.containsKey(imgID)) {
                                Img img = imgMap.get(imgID);
                                img.add(jsonObject.get("index").getAsInt(), jsonObject.get("data").getAsString());
                                imgMap.replace(imgID, img);
                            } else {
                                Img img = new Img(jsonObject.get("packageNum").getAsInt(), jsonObject.get("index").getAsInt(), jsonObject.get("data").getAsString());
                                imgMap.put(imgID, img);
                            }
                            Img img = imgMap.get(imgID);
                            if (img.allReceived()) {
                                Map<String, Object> msgq = new HashMap<>();
                                msgq.put("type", "img");
                                msgq.put("player", jsonObject.get("sender").getAsString());
                                msgq.put("msg", img.getData());
                                JSONObject jo = new JSONObject(msgq);
                                MsgTool.msgSend(Client.session, jo.toJSONString());

                                Object[] players = Chatsync.getPlugin(Chatsync.class).getServer().getOnlinePlayers().toArray();
                                ImgTools.sendImg("[" + jsonObject.get("sender").getAsString() + "]:", players, img.getData());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        getServer().getMessenger().registerOutgoingPluginChannel(this, channel);
        File configFile = new File(Chatsync.getPlugin(Chatsync.class).getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", true);
            this.getLogger().info("File: 已生成 config.yml 文件");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        Metrics metrics = new Metrics(this, 17411);
        Bukkit.getPluginCommand("qqmsg").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(new top.ncserver.chatsync.V2.Until.Listener(), this);

        c = new Client();
        c.runTaskAsynchronously(this);


        logger.info("聊天同步插件已加载");
        Object[] players = this.getServer().getOnlinePlayers().toArray();
        for (Object player : players) {
            ((Player) player).getPlayer().sendMessage("§a[消息同步]消息同步插件加载成功,当前版本:" + getPlugin(this.getClass()).getDescription().getVersion());
        }
    }

    @Override
    public void onDisable() {
        isOnDisable = true;
        c.cancel();
        try {
            Client.session.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("chatsync.qqmsg")) {
            commandSender.sendMessage("§c您没有这个命令的权限");
            return true;
        }
        if (strings.length == 0) {
            commandSender.sendMessage("§c这个命令需要参数！");
            return true;
        }
        logger.info("[发送消息]" + strings[0]);
        msg.clear();
        msg.put("type", "obRe");
        msg.put("msg", strings[0]);
        JSONObject jo = new JSONObject(msg);
        MsgTool.msgSend(Client.session, jo.toJSONString());

        return true;
    }



    private String read(byte[] array) {
        ByteBuf buf = Unpooled.wrappedBuffer(array);
        return buf.toString(StandardCharsets.UTF_8);

    }




}

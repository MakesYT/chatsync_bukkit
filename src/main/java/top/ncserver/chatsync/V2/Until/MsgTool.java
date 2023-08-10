package top.ncserver.chatsync.V2.Until;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;
import top.ncserver.chatsync.Chatsync;
import top.ncserver.chatsync.Client;
import top.ncserver.chatsync.Until.ConsoleSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MsgTool {
    private static boolean syncMsg = true;
    public static void msgRead(AioSession session, String msgJ) throws IOException {
        JSONObject jsonObject = JSONObject.parseObject(msgJ);
        Object[] players = Chatsync.getPlugin(Chatsync.class).getServer().getOnlinePlayers().toArray();
        Map<String, Object> msg = new HashMap<>();
        switch (jsonObject.getString("type")){
            case "remsg":{
                if (syncMsg) {
                    Chatsync.getPlugin(Chatsync.class).logger.info(jsonObject.getString("msg"));
                    for (Object player : players) {
                        ((Player) player).getPlayer().sendMessage(jsonObject.getString("msg"));
                    }
                }
                break;
            }
            case "msg":{
                if (syncMsg)
                {
                    String msg1 = Chatsync.config.getConfigurationSection("qqMsgStyle").getString(String.valueOf(jsonObject.getInteger("permission")));
                    msg1 = msg1.replace("%s%", jsonObject.getString("sender"));
                    msg1 = msg1.replace("%msg%", jsonObject.getString("msg"));
                    Chatsync.getPlugin(Chatsync.class).logger.info(msg1);
                    for (Object player : players) {
                        ((Player) player).getPlayer().sendMessage(msg1);
                    }


                }
                break;
            }
            case "img": {
                String msg1 = Chatsync.config.getConfigurationSection("qqMsgStyle").getString(String.valueOf(jsonObject.getInteger("permission")));
                msg1 = msg1.replace("%s%", jsonObject.getString("sender"));
                msg1 = msg1.replace("%msg%", "");
                Chatsync.getPlugin(Chatsync.class).logger.info("收到图片");
                ImgTools.sendImg(msg1, players, jsonObject.getString("data"));
            }
            case "command":{
                if (jsonObject.getString("command").equals("/ls")) {
                    Chatsync.getPlugin(Chatsync.class).logger.info("QQ群[" + jsonObject.getString("sender") + "]查询了玩家在线数量");
                    msg.put("type", "playerList");

                    StringBuilder listBuilder = new StringBuilder();
                    int invise=0;
                    for (Object player : players) {
                        if (((Player) player).getPlayer().hasPermission("chatsync.invisible")) {
                            invise++;
                            continue;
                        }
                        listBuilder.append(",").append(((Player) player).getPlayer().getDisplayName());
                    }
                    String list = listBuilder.toString();
                    if (list.length() > 0) {
                        msg.put("msg", list.substring(1));
                        msg.put("online", players.length-invise);
                    } else msg.put("msg", "无,惨兮兮");
                    JSONObject jo = new JSONObject(msg);
                    msgSend(session,jo.toJSONString());
                } else if (jsonObject.getString("command").equals("/ls!") ) {
                    Chatsync.getPlugin(Chatsync.class).logger.info("QQ群[" + jsonObject.getString("sender") + "]查询了所有玩家在线数量(无视权限)");
                    msg.put("type", "playerList");
                    msg.put("online", players.length);
                    StringBuilder listBuilder = new StringBuilder();
                    for (Object player : players) {
                        listBuilder.append(",").append(((Player) player).getPlayer().getDisplayName());
                    }
                    String list = listBuilder.toString();
                    if (list.length() > 0) {
                        msg.put("msg", list.substring(1));
                    } else msg.put("msg", "无,惨兮兮");
                    JSONObject jo = new JSONObject(msg);
                    msgSend(session, jo.toJSONString());
                } else {
                    Chatsync.getPlugin(Chatsync.class).logger.info("QQ群[" + jsonObject.getString("sender") + "]执行了" + jsonObject.getString("command"));
                    msg.put("type", "command");
                    Bukkit.getScheduler().runTaskAsynchronously(Chatsync.getPlugin(Chatsync.class), () -> {
                        String cmd = jsonObject.getString("command").substring(1);
                        ConsoleSender sender = new ConsoleSender(Bukkit.getServer());
                        Bukkit.getScheduler().runTask(Chatsync.getPlugin(Chatsync.class), () -> Bukkit.dispatchCommand(sender, cmd));
                    });
                }
                break;
            }
            case "init":{
                syncMsg= jsonObject.getBoolean("command");
                if (!syncMsg){
                    HandlerList.unregisterAll((Listener) Chatsync.getPlugin(Chatsync.class));
                }
                break;
            }
        }


    }
    public static void msgSend(AioSession session, String msg) {
        if (Client.isConnected)
            try{
                WriteBuffer writeBuffer = session.writeBuffer();
                byte[] data = msg.getBytes(Charsets.UTF_8);
                writeBuffer.writeInt(data.length);
                writeBuffer.write(data);
                writeBuffer.flush();
            }catch (IOException e){
            }


    }
}

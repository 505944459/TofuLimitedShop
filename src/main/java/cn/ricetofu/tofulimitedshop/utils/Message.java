package cn.ricetofu.tofulimitedshop.utils;

import org.bukkit.entity.Player;

/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 对玩家发送消息的类
 * */
public class Message {

    // 向玩家发送消息的前缀
    private static final String PREFIX = "§f[§a豆腐专卖店§f] ";

    /**
     * 发送一条普通消息
     * @param msg 消息
     * */
    public static void info(Player player,String msg){
        player.sendMessage(PREFIX+msg);
    }

    /**
     * 发送一条警告消息
     * @param msg 消息
     * */
    public static void warn(Player player,String msg){
        player.sendMessage(PREFIX+msg);
    }

    /**
     * 发送一条错误消息
     * @param msg 消息
     * */
    public static void error(Player player,String msg){
        player.sendMessage(PREFIX+msg);
    }

}

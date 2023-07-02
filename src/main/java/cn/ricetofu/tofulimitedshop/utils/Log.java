package cn.ricetofu.tofulimitedshop.utils;


import org.bukkit.Bukkit;

import java.util.logging.Logger;

/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 日志类
 * */
public class Log {

    // 日志记录对象
    private static Logger logger = Bukkit.getLogger();

    // 插件日志输出前缀
    private static final String PREFIX = "[TofuLimitedShop] ";

    /**
     * 记录一个普通信息日志
     * @param msg 需要输出的日志信息
     * */
    public static void info(String msg){
        logger.info(PREFIX+msg);
    }

    /**
     * 记录一次警告日志
     * @param msg 需要输出的日志信息
     * */
    public static void warn(String msg){
        logger.warning(PREFIX+msg);
    }

    /**
     * 记录一次错误日志
     * @param msg 需要输出的日志信息
     * */
    public static void error(String msg){
        logger.severe(PREFIX+msg);
    }

}

package cn.ricetofu.tofulimitedshop.schedule;

import cn.ricetofu.tofulimitedshop.core.manager.PlayerDataManager;
import cn.ricetofu.tofulimitedshop.utils.Log;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 自动保存定时任务
 * */
public class AutoSaveSchedule extends BukkitRunnable {

    @Override
    public void run() {
        Log.info("正在保存玩家数据");
        PlayerDataManager.saveAll(false,false);// 本身就是异步调用,不需要再嵌异步了
        Log.info("玩家数据保存完毕");
    }

}

package cn.ricetofu.tofulimitedshop.schedule;


import cn.ricetofu.tofulimitedshop.core.PlayerCommodity;
import cn.ricetofu.tofulimitedshop.core.manager.PlayerDataManager;
import cn.ricetofu.tofulimitedshop.utils.Log;
import cn.ricetofu.tofulimitedshop.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;



/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 每日的商品刷新定时任务
 * */
public class DailyRefreshSchedule extends BukkitRunnable {

    @Override
    public void run() {
        long l = System.currentTimeMillis();
        Log.info("正在执行商品刷新任务(异步任务).");

        // 对线上玩家进行逐个的商店刷新
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            boolean fresh = false;
            String uuid = player.getUniqueId().toString();
            for (PlayerCommodity value : PlayerDataManager.getByUuid(uuid).getCommodityMap().values()) {
                if (value.fresh()) {
                    fresh = true;
                }
            }
            if(fresh) Message.info(player,"你的商店出现了更新~~~输入:/lshop 查看");
        }
        Log.info("刷新完成,本次刷新耗时:"+(System.currentTimeMillis()-l)+"ms");
    }
}

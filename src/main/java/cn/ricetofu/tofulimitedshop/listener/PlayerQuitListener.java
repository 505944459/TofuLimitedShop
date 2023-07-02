package cn.ricetofu.tofulimitedshop.listener;

import cn.ricetofu.tofulimitedshop.core.manager.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author ricetofu
 * @date 2023-7-1
 * @description 玩家退出游戏事件监听
 * */
public class PlayerQuitListener implements Listener {

    @EventHandler
    public void playerQuit(PlayerQuitEvent event){
        // 保存玩家信息,并删除缓存
        PlayerDataManager.saveByUuid(event.getPlayer().getUniqueId().toString(),true);
    }

}

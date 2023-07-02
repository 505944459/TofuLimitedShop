package cn.ricetofu.tofulimitedshop.listener;

import cn.ricetofu.tofulimitedshop.core.manager.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author ricetofu
 * @date 2023-7-1
 * @description 玩家加入监听,加载玩家的数据
 * */
public class PlayerJoinListener implements Listener {

    @EventHandler
    public void joinListener(PlayerJoinEvent event){
        new Thread(()->PlayerDataManager.getByUuid(event.getPlayer().getUniqueId().toString())).start();
    }

}

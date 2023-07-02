package cn.ricetofu.tofulimitedshop.test;

import cn.ricetofu.tofulimitedshop.core.manager.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;




public class BreakListener implements Listener {

    @EventHandler
    public void breakListener(BlockBreakEvent event){

        PlayerDataManager.getByUuid(event.getPlayer().getUniqueId().toString());
    }

}

package cn.ricetofu.tofulimitedshop.command;

import cn.ricetofu.tofulimitedshop.core.gui.PlayerGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author ricetofu
 * @date 2023-7-1
 * @description 指令执行器
 * */
public class LShop implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        new Thread(()->PlayerGUI.openInv((Player) sender,"buy")).start();
        return true;
    }
}

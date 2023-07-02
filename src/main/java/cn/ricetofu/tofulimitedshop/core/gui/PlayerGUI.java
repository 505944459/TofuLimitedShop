package cn.ricetofu.tofulimitedshop.core.gui;

import cn.ricetofu.tofulimitedshop.TofuLimitedShop;
import cn.ricetofu.tofulimitedshop.core.Commodity;
import cn.ricetofu.tofulimitedshop.core.PlayerData;
import cn.ricetofu.tofulimitedshop.core.manager.CommodityManager;
import cn.ricetofu.tofulimitedshop.core.manager.PlayerDataManager;
import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlayerGUI implements Listener {


    /**
     * 玩家打开GUI的操作
     * @param player 玩家对象
     * @param type 类型:buy/sell
     * */
    public static void openInv(Player player,String type){

        // 创建一个 6*9 大小的箱子
        Inventory inventory= Bukkit.createInventory(player,6*9,"§a[§b豆腐专卖店§a]§f");

        // 先绘制GUI的边界
        ItemStack edgeGlassPane = XMaterial.CYAN_STAINED_GLASS_PANE.parseItem();
        ItemMeta edgeGlassPaneMeta = edgeGlassPane.getItemMeta();
        edgeGlassPaneMeta.setDisplayName("§f边界线");
        edgeGlassPane.setItemMeta(edgeGlassPaneMeta);
        // 绘制
        for (int i = 0; i < 6; i++) for (int j = 0; j < 9; j++) {
            if(i==0||i==5||j==0||j==8){
                //边界方块
                inventory.setItem(i*9+j,edgeGlassPane);
            }
        }

        // 绘制玩家头颅
        ItemStack skull = SkullUtils.getSkull(player.getUniqueId()); // 获取玩家头颅
        ItemMeta playerDataMate = skull.getItemMeta();
        playerDataMate.setDisplayName("§a"+player.getName()+"§f");
        List<String> skullLore = new ArrayList<>();
        skullLore.add("§f欢迎来到豆腐专卖店");
        skullLore.add("§f目前所在的商店是"+(type.equals("buy")?"§a出售":"§3收购")+"§f商店");

        if(TofuLimitedShop.moneyTypeSupport("vault")){
            skullLore.add("§a剩余"+TofuLimitedShop.getMoneyTypeAlias("vault")+"§f: §a"+TofuLimitedShop.getEconomy().getBalance(player));
        }

        if(TofuLimitedShop.moneyTypeSupport("points")){
            skullLore.add("§a剩余"+TofuLimitedShop.getMoneyTypeAlias("points")+"§f: §a"+TofuLimitedShop.getPlayerPoints().getAPI().look(player.getUniqueId()));
        }

        skullLore.add("§f切换商店按钮在那边--------->");
        playerDataMate.setLore(skullLore);
        skull.setItemMeta(playerDataMate);
        inventory.setItem(0,skull);

        // 绘制购买/收购切换按钮
        ItemStack switchButton = type.equals("buy")?XMaterial.TNT_MINECART.parseItem():XMaterial.HOPPER_MINECART.parseItem();
        ItemMeta switchButtonItemMeta = switchButton.getItemMeta();
        switchButtonItemMeta.setDisplayName("§a出售§f/§3收购§f商店切换");
        switchButtonItemMeta.setLore(Collections.singletonList("你目前在:" + (type.equals("buy") ? "§a出售" : "§3收购") + "§f商店"));
        switchButton.setItemMeta(switchButtonItemMeta);
        inventory.setItem(8,switchButton);
        // todo 绘制上下页按钮


        // 绘制商品
        List<Commodity> commodities = CommodityManager.getByType(type.equals("buy")?"sell":"buy");
        PlayerData playerData = PlayerDataManager.getByUuid(player.getUniqueId().toString());

        int i = 1; // 行
        int j = 1; // 列
        for (Commodity commodity : commodities) {

            ItemStack itemStack = new ItemStack(Material.matchMaterial(commodity.getItem())); // 显示Item
            itemStack.setAmount(commodity.getAmount()); // 显示数量

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§f"+commodity.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§a-------------------§f");
            for (String detail : commodity.getDetails()) lore.add("§f"+detail);
            lore.add("§a-------------------§f");
            lore.add("§f商品类型: "+(commodity.getType().equals("sell")?"§a出售":"§3收购"));
            lore.add((commodity.getType().equals("sell")?"§f出售":"§f收购")+"价格: §a"+commodity.getMoney());
            lore.add("§f货币类型: "+ TofuLimitedShop.getMoneyTypeAlias(commodity.getMoneyType()));
            String refresh = commodity.getRefresh();
            switch (refresh){
                case "once":{
                    refresh = "§c永不";
                    break;
                }
                case "daily":{
                    refresh = "§a每天";
                    break;
                }
                case "weekly":{
                    refresh = "§3每周";
                    break;
                }
                case "monthly":{
                    refresh = "§9每月";
                }
            }
            lore.add("§f刷新周期: "+refresh);
            lore.add("§f刷新时间: "+playerData.getCommodityMap().get(commodity.getId()).getLastRefreshDate());
            lore.add("§f限"+(commodity.getType().equals("sell")?"购":"售")+"次数: §a"+commodity.getLimit());
            lore.add("§f已"+(commodity.getType().equals("sell")?"购":"售")+"次数: §3"+playerData.getCommodityMap().get(commodity.getId()).getAlready());
            String can;
            if(playerData.getCommodityMap().get(commodity.getId()).getAlready() < commodity.getLimit()){
                can = "§a点击"+(commodity.getType().equals("sell")?"§a购买":"§3出售");
            }else can = "§c已达次数限制~";
            lore.add(can);
            lore.add("商品id:"+commodity.getId());
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            inventory.setItem(i+j*9,itemStack);
            i++;
            if(i >= 8){
                i = 1;
                j++;
            }
            if(j >= 5)break;
        }

        // 玩家打开箱子
        player.openInventory(inventory);
    }


    @EventHandler
    public void onClick(InventoryClickEvent event){
        InventoryView inventory = event.getWhoClicked().getOpenInventory();
        // 保护箱子GUI里的东西,并根据点击的东西进行操作判定
        if(!inventory.getTitle().equals("§a[§b豆腐专卖店§a]§f")){
            // 不是商店箱子GUI
            return;
        }
        event.setCancelled(true); // 总之先取消掉这次事件
        if(!event.getClick().isLeftClick() && !event.getClick().isShiftClick())return; // 不是左键点击,不触发点击操作

        ItemStack itemStack = event.getCurrentItem();
        if(itemStack == null ||itemStack.getType().equals(Material.AIR))return; // 点击的是空气
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta.getDisplayName().equals("§f边界线"))return; // 点击的是边界
        // 再来判断玩家点击的是啥
        // 切换商店按钮
        if(itemMeta.getDisplayName().equals("§a出售§f/§3收购§f商店切换")){
            event.getWhoClicked().closeInventory();
            openInv((Player) event.getWhoClicked(),(itemMeta.getLore().get(0).equals("你目前在:§a出售§f商店")?"sell":"buy"));
            return;
        }
        // todo 上一页按钮


        // todo 下一页按钮


        // 点击的是商品
        String s = itemMeta.getLore().get(itemMeta.getLore().size() - 1);
        String id = s.substring(5); // 这个拿到的是商品的id
        // 购买或出售操作
        if (CommodityManager.buyOrSell((Player) event.getWhoClicked(),id)) {
            // 购买成功了需要更新商品的Lore呢
            Commodity commodity = CommodityManager.getById(id);
            itemStack.setAmount(commodity.getAmount()); // 显示数量
            itemMeta.setDisplayName("§f"+commodity.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§a-------------------§f");
            for (String detail : commodity.getDetails()) lore.add("§f"+detail);
            lore.add("§a-------------------§f");
            lore.add("§f商品类型: "+(commodity.getType().equals("sell")?"§a出售":"§3收购"));
            lore.add((commodity.getType().equals("sell")?"§f出售":"§f收购")+"价格: §a"+commodity.getMoney());
            lore.add("§f货币类型: "+ TofuLimitedShop.getMoneyTypeAlias(commodity.getMoneyType()));
            String refresh = commodity.getRefresh();
            switch (refresh){
                case "once":{
                    refresh = "§c永不";
                    break;
                }
                case "daily":{
                    refresh = "§a每天";
                    break;
                }
                case "weekly":{
                    refresh = "§3每周";
                    break;
                }
                case "monthly":{
                    refresh = "§9每月";
                }
            }
            PlayerData playerData = PlayerDataManager.getByUuid(event.getWhoClicked().getUniqueId().toString());
            lore.add("§f刷新周期: "+refresh);
            lore.add("§f刷新时间: "+playerData.getCommodityMap().get(commodity.getId()).getLastRefreshDate());
            lore.add("§f限"+(commodity.getType().equals("sell")?"购":"售")+"次数: §a"+commodity.getLimit());
            lore.add("§f已"+(commodity.getType().equals("sell")?"购":"售")+"次数: §3"+playerData.getCommodityMap().get(commodity.getId()).getAlready());
            String can;
            if(playerData.getCommodityMap().get(commodity.getId()).getAlready() < commodity.getLimit()){
                can = "§a点击"+(commodity.getType().equals("sell")?"§a购买":"§3出售");
            }else can = "§c已达次数限制~";
            lore.add(can);
            lore.add("商品id:"+commodity.getId());
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
    }
}

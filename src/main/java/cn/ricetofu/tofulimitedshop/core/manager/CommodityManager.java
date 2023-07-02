package cn.ricetofu.tofulimitedshop.core.manager;


import cn.ricetofu.tofulimitedshop.TofuLimitedShop;
import cn.ricetofu.tofulimitedshop.core.Commodity;
import cn.ricetofu.tofulimitedshop.core.PlayerCommodity;
import cn.ricetofu.tofulimitedshop.core.PlayerData;
import cn.ricetofu.tofulimitedshop.utils.Log;
import cn.ricetofu.tofulimitedshop.utils.Message;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 商品管理器
 * */
public class CommodityManager {

    // 所有商品表
    private static Map<String, Commodity> commodityMap = new ConcurrentHashMap<>();

    /**
     * 初始化商品管理器
     * @param configFolder 商品配置文件夹
     * @return 是否成功
     * */
    public static boolean init(File configFolder){
        Log.info("开始加载配置文件");
        // 过滤非配置文件
        File[] files = configFolder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        FileConfiguration config = new YamlConfiguration();
        Log.info("共扫描到:"+(files == null ? 0 : files.length)+"个配置文件,开始加载");
        if(files == null)return true; // 无配置文件
        // 一个一个加载配置文件
        for (File file : files) {
            try {
                config.load(file);
            } catch (Exception e){
                Log.error("配置文件:"+file.getName()+"加载失败,请检查内容格式!");
                continue;
            }

            // 每个id是一个独立的任务,循环遍历加载
            Set<String> ids = config.getKeys(false);
            for (String id : ids) {
                if(commodityMap.containsKey(id)){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,重复的任务id!");
                    commodityMap.remove(id);
                    continue;
                }
                // 参数加载和校验
                ConfigurationSection section = config.getConfigurationSection(id);
                String name = section.getString("name");
                if(name == null || name.equals("")){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,缺少name参数!");
                    continue;
                }
                List<String> details = section.getStringList("details");
                if(details == null || details.size() == 0){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,details参数至少应该有一行!");
                }
                String type = section.getString("type");
                if(type == null || type.equals("")){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,缺少type参数!");
                    continue;
                }
                if(!type.equals("sell") && !type.equals("buy")){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,type参数错误!应该为buy或sell!");
                    continue;
                }
                Integer limit = 0;
                try {
                    limit = section.getInt("limit");
                } catch (Exception e) {
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,limit参数错误!应该为一个正整数!");
                    continue;
                }
                if(limit <= 0){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,limit参数错误!应该为一个正整数!");
                    continue;
                }
                String refresh = section.getString("refresh");
                if(refresh == null || refresh.equals("")){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,缺少refresh参数!");
                    continue;
                }
                if(!refresh.equals("daily") && !refresh.equals("weekly") && !refresh.equals("monthly") && !refresh.equals("once")){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,refresh参数错误!应该为以下参数中的一个:");
                    Log.error("     daily,weekly,monthly,once");
                    continue;
                }
                String item = section.getString("item");
                if(item == null || item.equals("")){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,缺少item参数!");
                    continue;
                }
                if(Material.matchMaterial(item) == null){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,item参数错误!无法匹配一个名为:"+item+"的物品!");
                    continue;
                }
                Integer amount = 0;
                try {
                    amount = section.getInt("amount");
                } catch (Exception e) {
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,amount参数错误!应该为一个正整数!");
                    continue;
                }
                if(amount <= 0){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,amount参数错误!应该为一个正整数!");
                    continue;
                }
                String money_type = section.getString("money-type");
                if(money_type == null || money_type.equals("")){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,缺少money-type参数!");
                    continue;
                }
                if(!TofuLimitedShop.moneyTypeSupport(money_type)){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,错误的money-type参数!不支持:"+money_type+"类型的经济!");
                    continue;
                }
                Double money = 0d;
                try {
                    money = section.getDouble("money");
                } catch (Exception e) {
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,money参数错误!应该为一个正数!");
                    continue;
                }
                if(money <= 0){
                    Log.error("配置文件:"+file.getName()+"中的id为:"+id+"的任务加载失败,money参数错误!应该为一个正数!");
                    continue;
                }

                Commodity commodity = new Commodity();
                commodity.setId(id);
                commodity.setName(name);
                commodity.setDetails(details);
                commodity.setType(type);
                commodity.setLimit(limit);
                commodity.setRefresh(refresh);
                commodity.setItem(item);
                commodity.setAmount(amount);
                commodity.setMoneyType(money_type);
                commodity.setMoney(money);
                commodityMap.put(id,commodity);
            }
            Log.info("配置文件:"+file.getName()+"加载完成");
        }
        return true;
    }

    /**
     * 通过id获取一个商品
     * @param id 商品id
     * @return 商品实体
     * */
    public static Commodity getById(String id){
        return commodityMap.get(id);
    }

    /**
     * 通过商品类型获取一个商品
     * @param type 商品类型
     * @return 商品实体列表
     * */
    public static List<Commodity> getByType(String type){
        Collection<Commodity> values = commodityMap.values();
        List<Commodity> result = new ArrayList<>();
        for (Commodity value : values) {
            if(value.getType().equals(type))result.add(value);
        }
        return result;
    }

    /**
     * 获取所有商品列表
     * @return 商品实体列表
     * */
    public static List<Commodity> getAll(){
        return new ArrayList<>(commodityMap.values());
    }

    /**
     * 获取商品列表
     * @return 商品列表
     * */
    public static Map<String,Commodity> getCommodityMap(){
        return commodityMap;
    }

    public static boolean buyOrSell(Player player, String commodityId){
        Commodity commodity = commodityMap.get(commodityId);
        if(commodity == null)return false;

        if(commodity.getType().equals("sell"))return buy(player,commodity);
        else return sell(player,commodity);
    }

    private static boolean buy(Player player, Commodity commodity){
        if (!commodity.getType().equals("sell")) {
            Message.error(player,"购买时出现了错误");
            return false;
        }

        // 检查次数上限
        PlayerData playerData = PlayerDataManager.getByUuid(player.getUniqueId().toString());
        PlayerCommodity playerCommodity = playerData.getCommodityMap().get(commodity.getId());
        if(playerCommodity.getAlready() >= commodity.getLimit()){
            Message.warn(player,"已达到购买上限!");
            return false;
        }
        // 检查money是否足够
        String moneyType = commodity.getMoneyType();
        if(moneyType.equals("vault")){
            Economy economy = TofuLimitedShop.getEconomy();
            double balance = economy.getBalance(player);
            if (!(balance >= commodity.getMoney())) {
                Message.warn(player,"你的"+TofuLimitedShop.getMoneyTypeAlias(moneyType)+"§f不足");
                Message.warn(player,"需要:§a"+commodity.getMoney()+"§f而你只有:§3"+balance);
                return false;
            }
            // 支付
            economy.withdrawPlayer(player, commodity.getMoney());
        }else if(moneyType.equals("points")){
            PlayerPointsAPI api = TofuLimitedShop.getPlayerPoints().getAPI();
            double points = api.look(player.getUniqueId());
            if(!(points >= commodity.getMoney())){
                Message.warn(player,"你的"+TofuLimitedShop.getMoneyTypeAlias(moneyType)+"§f不足");
                Message.warn(player,"需要:§a"+commodity.getMoney()+"§f而你只有:§3"+points);
                return false;
            }
            // 支付
            api.take(player.getUniqueId(),commodity.getMoney().intValue());
        }
        boolean success = Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                "give %player% %item% %amount%"
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%amount%", commodity.getAmount() + "")
                        .replaceAll("%item%",commodity.getItem())
        );
        if(success){
            // 更新已购次数
            playerCommodity.setAlready(playerCommodity.getAlready() + 1);
            Message.info(player,"购买成功");
            return true;
        }else {
            // todo 退款操作
            Message.info(player, "购买失败");
            return false;
        }
    }

    private static boolean sell(Player player, Commodity commodity){
        if (!commodity.getType().equals("buy")) {
            Message.error(player,"出售时出现了错误");
            return false;
        }
        // 检查次数上限
        PlayerData playerData = PlayerDataManager.getByUuid(player.getUniqueId().toString());
        PlayerCommodity playerCommodity = playerData.getCommodityMap().get(commodity.getId());
        if(playerCommodity.getAlready() >= commodity.getLimit()){
            Message.warn(player,"已达到售卖上限!");
            return false;
        }
        // 检查玩家是否有这个物品
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> itemStacks = Arrays.asList(inventory.getContents());
        Material material = Material.matchMaterial(commodity.getItem());
        List<ItemStack> right = new ArrayList<>();
        int have = 0;
        for (ItemStack itemStack : itemStacks) {
            if(itemStack == null) continue;
            if(itemStack.getType().equals(material)){
                right.add(itemStack);
                have += itemStack.getAmount();
            }
        }
        if(have < commodity.getAmount()){
            Message.warn(player,"你没有足够数量的物品.需要:§a"+commodity.getAmount()+"§f个,而你只有:§3"+have+"§f个.");
            return false;
        }
        // 拿走玩家的item
        int need = commodity.getAmount();
        for (ItemStack itemStack : right) {
            if(need == 0)break;
            if(itemStack.getAmount() <= need){
                need -= itemStack.getAmount();
                inventory.remove(itemStack);
            }
            else {
                itemStack.setAmount(itemStack.getAmount() - need);
                need = 0;
            }
        }
        // 然后在支付给玩家money
        if(commodity.getMoneyType().equals("vault")){
            TofuLimitedShop.getEconomy().depositPlayer(player,commodity.getMoney());
        }else if(commodity.getMoneyType().equals("points")){
            TofuLimitedShop.getPlayerPoints().getAPI().give(player.getUniqueId(),commodity.getMoney().intValue());
        }
        playerCommodity.setAlready(playerCommodity.getAlready() + 1);
        Message.info(player,"出售成功,你获得了: §a"+commodity.getMoney()+TofuLimitedShop.getMoneyTypeAlias(commodity.getMoneyType()));
        return true;
    }
}

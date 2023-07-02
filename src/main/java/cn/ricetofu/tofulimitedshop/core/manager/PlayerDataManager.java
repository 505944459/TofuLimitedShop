package cn.ricetofu.tofulimitedshop.core.manager;

import cn.ricetofu.tofulimitedshop.core.Commodity;
import cn.ricetofu.tofulimitedshop.core.PlayerCommodity;
import cn.ricetofu.tofulimitedshop.core.PlayerData;
import cn.ricetofu.tofulimitedshop.utils.Log;
import cn.ricetofu.tofulimitedshop.utils.Message;
import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ricetofu
 * @date 2023-7-1
 * @description 数据管理器
 * */
public class PlayerDataManager {

    // 玩家数据内存缓存表 key: uuid value: 数据 (存在异步多线程操作,所以用了这个线程安全的Map)
    private static Map<String, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    // 玩家数据存在的物理位置
    private static File data = null;

    // json解析镀锡
    private static Gson gson = new Gson();

    /**
     * 初始化数据管理器
     * @param dataFolder 商品配置文件夹
     * @return 是否成功
     * */
    public static boolean init(File dataFolder){
        if(!dataFolder.exists()){
            // 不存在数据文件夹,先创建一个
            if(!dataFolder.mkdir()){
                Log.error("创建玩家数据文件夹失败!");
                return false;
            }
        }
        data = dataFolder;
        return true;
    }

    /**
     * 根据uuid获取单个玩家的数据(内存缓存)
     * @param uuid 玩家的uuid
     * @return 玩家数据对象
     * */
    public static PlayerData getByUuid(String uuid){
        // 缓存命中,直接返回
        if(playerDataMap.containsKey(uuid))return playerDataMap.get(uuid);

        // 无缓存,则从本地数据文件中加载
        File play_data = new File(data,uuid+".json");
        PlayerData playerData = null;
        if(!play_data.exists()){
            // 玩家数据文件夹不存在,则证明玩家是第一次进入捏
            playerData = new PlayerData();
            playerData.setUuid(uuid);
            playerData.setCommodityMap(new HashMap<>());
            // 将所有商品放入玩家的数据里面
            Collection<Commodity> all = CommodityManager.getAll();
            for (Commodity commodity : all) {
                PlayerCommodity playerCommodity = new PlayerCommodity();
                playerCommodity.setAlready(0);
                playerCommodity.setId(commodity.getId());
                playerCommodity.setRefresh(new Date());
                playerData.getCommodityMap().put(playerCommodity.getId(),playerCommodity);
            }
            Message.info(Bukkit.getPlayer(UUID.fromString(uuid)),"你的商店出现了更新~~~输入:/lshop 查看");
        }else {
            try {
                InputStreamReader reader = new InputStreamReader(Files.newInputStream(play_data.toPath()), StandardCharsets.UTF_8);
                playerData = gson.fromJson(reader,PlayerData.class);
                reader.close();
            }catch (Exception e){
                Log.error("读取玩家:"+uuid+"的数据时出现了一个错误!");
            }

            // 检验一下玩家数据里的商品信息,是否存在已经不存在的商品(配置删除)
            Collection<PlayerCommodity> values = playerData.getCommodityMap().values();
            Map<String, Commodity> commodityMap = CommodityManager.getCommodityMap();
            List<String> removeList = new ArrayList<>(); // 需要删掉的不正确的商品
            for (PlayerCommodity value : values) {
                if (!commodityMap.containsKey(value.getId())) {
                    removeList.add(value.getId());
                }
            }
            for (String s : removeList) { // 删掉
                playerData.getCommodityMap().remove(s);
            }
            // 再次检验是否存在未添加的商品(新增配置)
            for (String s : commodityMap.keySet()) {
                if(!playerData.getCommodityMap().containsKey(s)){
                    PlayerCommodity commodity = new PlayerCommodity();
                    commodity.setId(s);
                    commodity.setRefresh(new Date());
                    commodity.setAlready(0);
                    playerData.getCommodityMap().put(s,commodity);
                }
            }

            // 检验玩家的任务是否需要刷新
            boolean fresh = false;
            for (PlayerCommodity value : values) {
                if (value.fresh()) {
                    fresh = true;
                }
            }
            if(fresh) Message.info(Bukkit.getPlayer(UUID.fromString(uuid)),"你的商店出现了更新~~~输入:/lshop 查看");
        }
        playerDataMap.put(uuid,playerData); // 缓存
        return playerData;
    }

    /**
     * 获取内存缓存中存在的所有的玩家信息
     * @return 内存缓存表
     * */
    public static Map<String,PlayerData> getAllCached(){
        return playerDataMap;
    }

    /**
     * 保存某个玩家的数据至本地
     * @param uuid 玩家的uuid
     * @param removeCache 是否移除缓存,玩家离线清除缓存,省内存
     * @return 是否成功
     * */
    public static boolean saveByUuid(String uuid,boolean removeCache){
        // 先保存
        PlayerData playerData = playerDataMap.get(uuid);
        if(playerData == null) return false;

        File player_date = new File(data,uuid+".json");
        //文件不存在则创建一个
        if(!player_date.exists()) {
            try {
                if (!player_date.createNewFile()) {
                    Log.error("创建玩家:"+uuid+"数据文件时出现了一个错误!");
                    return false;
                }
            } catch (IOException e) {
                Log.error("创建玩家:"+uuid+"数据文件时出现了一个错误!");
                return false;
            }
        }

        String s = gson.toJson(playerData);
        try{
            OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(player_date.toPath()), StandardCharsets.UTF_8);
            writer.write(s);
            writer.flush();
            writer.close();
        }catch (Exception e){
            Log.error("保存玩家:"+uuid+"的数据时出现了一个错误!");
            return false;
        }

        if(removeCache)playerDataMap.remove(uuid);// 清楚cache

        return true;
    }

    /**
     * 保存所有玩家的数据
     * @param removeCache 是否需要删除缓存
     * @param asynchronous 是否异步
     * */
    public static boolean saveAll(boolean removeCache,boolean asynchronous){
        if(asynchronous) new Thread(()->{
            for (String s : playerDataMap.keySet()) saveByUuid(s,removeCache);
        }).start();
        else for (String s : playerDataMap.keySet()) saveByUuid(s,removeCache);
        return true;
    }

}

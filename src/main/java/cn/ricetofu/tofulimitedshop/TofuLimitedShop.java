package cn.ricetofu.tofulimitedshop;

import cn.ricetofu.tofulimitedshop.command.LShop;
import cn.ricetofu.tofulimitedshop.core.gui.PlayerGUI;
import cn.ricetofu.tofulimitedshop.core.manager.CommodityManager;
import cn.ricetofu.tofulimitedshop.core.manager.PlayerDataManager;
import cn.ricetofu.tofulimitedshop.listener.PlayerJoinListener;
import cn.ricetofu.tofulimitedshop.listener.PlayerQuitListener;
import cn.ricetofu.tofulimitedshop.schedule.AutoSaveSchedule;
import cn.ricetofu.tofulimitedshop.schedule.DailyRefreshSchedule;
import cn.ricetofu.tofulimitedshop.utils.Log;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 插件主类
 * */
public final class TofuLimitedShop extends JavaPlugin {

    // Vault 插件
    private static Economy economy = null;
    // PlayerPoints 插件
    private static PlayerPoints playerPoints = null;

    // 经济类型别名
    private static String vault_alias = "§6金币";
    private static String points_alias = "§3点券";

    // 是否检查更新
    private static boolean check_update = false;

    // 是否自动保存玩家数据
    private static boolean auto_save = true;
    private static int auto_save_timer = 60; // 时间周期(单位:分钟)

    @Override
    public void onEnable() {
        long l = System.currentTimeMillis();
        Log.info("插件正在加载");
        saveDefaultConfig();// 保存默认的配置文件
        File config_file = new File(getDataFolder(),"./config");
        if(!config_file.exists()){
            // 不存在数据文件夹,输出默认的示例数据
            if (!config_file.mkdir()) {
                Log.error("创建默认的数据文件夹失败!");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            config_file = new File(config_file,"default.yml");
            InputStream in = getResource("default.yml");
            OutputStream out = null;
            try {
                out = Files.newOutputStream(config_file.toPath());
                byte[] buf = new byte[1024*1024];
                int n;
                while ((n = in.read(buf)) > 0){
                    out.write(buf,0,n);
                }
                out.flush();
                out.close();
                in.close();
            }catch (Exception e){
                Log.error("输出默认配置文件时出现了错误!");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        // 读取config.yml下的配置内容
        FileConfiguration config = getConfig();
        try {
            vault_alias = config.getString("vault-alias");
            points_alias = config.getString("points-alias");
            check_update = config.getBoolean("update-check");
            auto_save = config.getBoolean("auto-save");
            auto_save_timer = config.getInt("auto-save-timer");
        }catch (Exception e){
            Log.error("读取主配置文件config.yml出错,配置正确?无法解决请删除config.yml重启重新生成.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Log.info("读取config.yml完毕");

        // 检查Vault类插件是否安装
        RegisteredServiceProvider<Economy> registration = getServer().getServicesManager().getRegistration(Economy.class);
        if(registration == null){
            Log.error("插件未发现Vault系列插件(没有经济插件,或是只装了Vault前置??)");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        economy = registration.getProvider();
        if(economy == null){
            Log.error("注册Vault插件失败");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }else Log.info("Vault插件注册成功");

        // 检查playerPoints是否安装
        Plugin plugin = getServer().getPluginManager().getPlugin("PlayerPoints");
        playerPoints = PlayerPoints.class.cast(plugin);
        if(playerPoints == null){
            Log.info("未发现PlayerPoints插件,将不支持PlayerPoints经济类型");
        }else Log.info("PlayerPoints插件注册成功");

        // 初始化核心组件管理器
        // 商品管理器
        if (!CommodityManager.init(new File(getDataFolder(),"./config"))) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // 玩家数据管理器
        if (!PlayerDataManager.init(new File(getDataFolder(),"./data"))) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 事件注册
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this); // 玩家加入事件
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(),this); // 玩家退出事件
        Bukkit.getPluginManager().registerEvents(new PlayerGUI(),this); // 玩家GUI交互相关的事件

        // 进行指令注册
        Bukkit.getPluginCommand("lshop").setExecutor(new LShop()); // 主要指令

        // 更新检查
        if(check_update){
            Log.info("正在进行更新检查");
            // todo 更新检查代码

            // todo
            Log.info("目前已经是最新版本");
        }

        // 自动保存任务
        if(auto_save){
            new AutoSaveSchedule().runTaskTimerAsynchronously(this,auto_save_timer*60L*1000,auto_save_timer*60L*1000);
        }

        // 开启每日定时刷新商店物资
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String last = format.format(new Date(now.getTime() + 24L * 60 * 60 * 1000)); // 明天日期的时间字符串
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse = null;
        try {
            parse = format.parse(last + " 00:00:00");
        } catch (ParseException e) {
            Log.error("定时任务设置失败");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        new DailyRefreshSchedule().runTaskTimerAsynchronously(this,parse.getTime()-now.getTime(),24L*60*60*1000);

        // 开启bStats的插件统计功能
        new Metrics(this,18948);
        Log.info("加载成功,本次加载耗时:"+(System.currentTimeMillis()-l)+"ms.");
    }

    /**
     * 获取Vault操作对象
     * */
    public static Economy getEconomy(){
        return economy;
    }

    /**
     * 获取PlayerPoints操作对象
     * */
    public static PlayerPoints getPlayerPoints(){
        return playerPoints;
    }

    /**
     * 判断插件是否支持指定的经济类型
     * @param name 经济类型名
     * @return 是否支持
     * */
    public static boolean moneyTypeSupport(String name){
        if(name.equals("vault") && economy != null) return true;
        if(name.equals("points") && playerPoints != null)return true;
        return false;
    }

    /**
     * 获取经济类型别名
     * @param moneyType 经济类型
     * @return 别名
     * */
    public static String getMoneyTypeAlias(String moneyType){
        if(moneyType.equals("vault"))return vault_alias;
        if(moneyType.equals("points"))return points_alias;
        return "unknown";
    }

    @Override
    public void onDisable() {
        // 保存所有玩家的信息
        Log.info("正在保存玩家数据");
        PlayerDataManager.saveAll(true,false);
        Log.info("保存玩家数据完毕");
    }
}

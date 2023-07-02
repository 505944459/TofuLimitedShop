package cn.ricetofu.tofulimitedshop.core;

import cn.ricetofu.tofulimitedshop.core.manager.CommodityManager;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 代表玩家的商品信息
 * */
@Data
public class PlayerCommodity {

    // 商品的id
    private String id;

    // 商品已经购买/出售过的次数
    private Integer already;

    // 商品上次刷新的时间
    private Date refresh;


    /**
     * 尝试进行一次商品刷新(如果满足刷新条件的话)
     * @return 是否成功
     * */
    public boolean fresh(){
        Commodity commodity = CommodityManager.getById(id);
        if(commodity.getRefresh().equals("once"))return false; // 一次性任务,永远不刷新

        switch (commodity.getRefresh()){
            case "daily":{
                String last = format.format(refresh);// 上次刷新的日期
                String now = format.format(new Date());// 现在的日期
                if(!now.equals(last)){
                    // 不是同一个日期,则可以直接刷新日常任务
                    already = 0;
                    return true;
                }
                return false;
            }
            case "weekly":{
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(refresh);
                int i = calendar.get(Calendar.DAY_OF_WEEK);
                i = i == 1 ? 7 : i-1; // 上次刷新的日子是星期几
                Calendar now = Calendar.getInstance();
                calendar.setTime(new Date());
                int j = now.get(Calendar.DAY_OF_WEEK);
                j = j == 1 ? 7 : j-1; // 现在是星期几
                if( j < i ){
                    // 现在的星期数小于上一次刷新,说明100%跨越了一个星期
                    already = 0;
                    return true;
                }
                // 判断两天的差距是否大于1星期,大于则证明应该刷新,小于则证明还在一个星期内
                if (calendar.getTime().getTime() - now.getTime().getTime() >= 7L * 24 * 60 * 60 * 1000) {
                    already = 0;
                    return true;
                }
                return false;
            }
            case "monthly":{
                String last = format.format(refresh);
                String now = format.format(new Date());
                String[] lasts = last.split("-");
                String[] nows = now.split("-");
                if(lasts[0].equals(nows[0]) && lasts[1].equals(nows[1]))return false; // 判断是否是同年同月
                already = 0;
                return true;
            }
        }
        return false;
    }
    // 格式转换器
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * 获取下次刷新的日期
     * @return yyyy-MM-dd格式的字符串
     * */
    public String getLastRefreshDate(){
        Commodity commodity = CommodityManager.getById(id);
        if(commodity.getRefresh().equals("once"))return "2999-12-12";
        switch (commodity.getRefresh()){
            case "daily":{
                return format.format(new Date(refresh.getTime() + (24L * 60 * 60 * 1000)));
            }
            case "weekly":{
                Calendar last = Calendar.getInstance();
                last.setTime(new Date(refresh.getTime() + 24L * 60 * 60 * 1000));
                while (last.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
                    last.setTime(new Date(last.getTime().getTime() + 24L * 60 * 60 * 1000));
                }
                return format.format(last.getTime());
            }
            case "monthly":{
                String[] last = format.format(refresh).split("-");
                int year = Integer.parseInt(last[0]);
                int month = Integer.parseInt(last[1]);
                int day = 1;
                month = month == 12 ? 1 : month + 1;
                year = month == 1 ? year + 1 : year;
                return year+"-"+month+"-"+day;
            }
        }

        return "known";
    }

}

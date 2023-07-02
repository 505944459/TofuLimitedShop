package cn.ricetofu.tofulimitedshop.core;

import lombok.Data;

import java.util.Map;

/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 代表一个玩家的信息
 * */
@Data
public class PlayerData {

    // 玩家的uuid(唯一标识符)
    private String uuid;

    // 玩家的商品信息 key: 商品id value: 商品信息
    private Map<String, PlayerCommodity> commodityMap;

}

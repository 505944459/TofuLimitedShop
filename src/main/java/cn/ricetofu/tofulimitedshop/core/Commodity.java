package cn.ricetofu.tofulimitedshop.core;

import lombok.Data;

import java.util.List;

/**
 * @author ricetofu
 * @date 2023-6-30
 * @description 代表一个商品的信息
 * */
@Data
public class Commodity {

    // 商品的唯一id
    private String id;

    // 商品的名称
    private String name;

    // 商品的描述信息
    private List<String> details;

    // 商品的售卖类型
    private String type;

    // 商品的售卖限制次数
    private Integer limit;

    // 商品的刷新周期
    private String refresh;

    // 商品的物品id
    private String item;

    // 商品的单次售卖数量
    private Integer amount;

    // 商品的货币类型
    private String moneyType;

    // 商品的货币花费/价值
    private Double money;

}

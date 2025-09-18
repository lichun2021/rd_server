package com.hawk.game.config;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.ItemInfoCollection;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.List;

/**
 * 量子槽位等级表
 */
@HawkConfigManager.XmlResource(file = "xml/armour_quantum_consume.xml")
public class ArmourQuantumConsumeCfg extends HawkConfigBase {
    /**
     * 装备量子槽位等级
     */
    @Id
    protected final int quantumLevel;

    /**
     * 等级对应的战力
     */
    protected final int power;

    /**
     * 升级到本级的消耗
     */
    protected final String consumption;



    /**
     * 分解获取
     */
    protected final String resolve;

    /**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;

    /**
     * 分解获取
     */
    private ItemInfoCollection reolveItems;

    public ArmourQuantumConsumeCfg(){
        quantumLevel = 0;
        power = 0;
        consumption = "";
        resolve = "";
        atkAttr = "";
        hpAttr = "";
    }

    @Override
    protected boolean assemble() {
        reolveItems = ItemInfoCollection.valueOf(resolve);
        return true;
    }


    public int getPower() {
        return power;
    }

    public List<ItemInfo> getConsume(){
        return ItemInfo.valueListOf(consumption);
    }

    public ItemInfoCollection getReolveItems() {
        return reolveItems;
    }

    public int getAtkAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
    }

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }
}

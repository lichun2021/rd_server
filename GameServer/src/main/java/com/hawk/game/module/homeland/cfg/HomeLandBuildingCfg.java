package com.hawk.game.module.homeland.cfg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家园建筑配置
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "xml/homeland_build.xml")
public class HomeLandBuildingCfg extends HawkConfigBase {
    @Id
    protected final int buildId;

    protected final int prosperity;

    protected final int buildLevel;

    protected final int buildType;

    protected final String buildCost;
    //buff
    protected final String attr;

    //下一等级
    protected final int postStage;

    //拆解物品
    protected final String buildSplit;

    // 产资源每min
    protected final int getCurrencyRate;
    //最大产出
    protected final long currencyMaxNum;
    //战力
    protected final int power;

    //升级受繁荣度限制
    protected final int buildProsperityLimit;

    private final int getCurrencyCd;

    // 建筑作用号
    private Map<Const.EffType, Integer> buildEffectMap = new HashMap<>();

    protected List<ItemInfo> costItems;

    // 建筑拆解返还
    protected List<ItemInfo> reclaimItems;

    public HomeLandBuildingCfg() {
        buildId = 0;
        buildLevel = 0;
        prosperity = 0;
        buildType = 0;
        attr = "";
        postStage = 0;
        buildSplit = "";
        buildCost = "";
        getCurrencyRate = 0;
        currencyMaxNum = 0;
        power = 0;
        buildProsperityLimit = 0;
        getCurrencyCd = 0;
    }

    public int getPostStage() {
        return postStage;
    }

    public int getId() {
        return buildId;
    }

    public int getLevel() {
        return buildLevel;
    }

    public int getBuildType() {
        return buildType;
    }

    @Override
    protected boolean assemble() {
        // 升级消耗
        if (!HawkOSOperator.isEmptyString(buildCost)) {
            List<ItemInfo> costItemTemp = new ArrayList<>();
            String[] cost = buildCost.split(",");
            for (String info : cost) {
                ItemInfo item = new ItemInfo();
                if (!item.init(info)) {
                    throw new RuntimeException("Building cfg cost error " + buildId);
                }
                costItemTemp.add(item);
            }
            costItems = ImmutableList.copyOf(costItemTemp);
        }
        // 初始化建筑升级带来的作用号的影响
        Map<Const.EffType, Integer> buildEffectMapTemp = new HashMap<>();
        if (!HawkOSOperator.isEmptyString(attr)) {
            String[] attrStr = attr.split(",");
            for (String str : attrStr) {
                String[] idVal = str.split("_");
                if (idVal.length < 2) {
                    return false;
                }
                Const.EffType effType = Const.EffType.valueOf(Integer.parseInt(idVal[0]));
                if (effType == null) {
                    continue;
                }
                buildEffectMapTemp.put(Const.EffType.valueOf(Integer.parseInt(idVal[0])), Integer.parseInt(idVal[1]));
            }
        }
        this.buildEffectMap = ImmutableMap.copyOf(buildEffectMapTemp);
        if (!HawkOSOperator.isEmptyString(buildSplit)) {
            List<ItemInfo> reclaimItemTemp = new ArrayList<>();
            String[] cost = buildSplit.split(",");
            for (String info : cost) {
                ItemInfo item = new ItemInfo();
                if (!item.init(info)) {
                    throw new RuntimeException("Building cfg cost error " + buildId);
                }
                reclaimItemTemp.add(item);
            }
            reclaimItems = ImmutableList.copyOf(reclaimItemTemp);
        }
        return true;
    }

    public List<ItemInfo> getReclaimItems() {
        return reclaimItems;
    }

    public List<ItemInfo> getCostItem() {
        return costItems;
    }

    @Override
    protected boolean checkValid() {
        HomeLandBuildingTypeCfg typeCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingTypeCfg.class, buildType);
        if (typeCfg == null) {
            throw new RuntimeException("HomeLandBuildingTypeCfg not found buildType = " + buildType);
        }
        return true;
    }

    public Map<Const.EffType, Integer> getEffectMap() {
        return buildEffectMap;
    }

    public int getResPerHour() {
        return getCurrencyRate;
    }

    public long getResLimit() {
        return currencyMaxNum;
    }

    public int getPower() {
        return power;
    }

    public int getProsperity() {
        return prosperity;
    }

    public int getBuildProsperityLimit() {
        return buildProsperityLimit;
    }

    public int getGetCurrencyCd() {
        return getCurrencyCd * 1000;
    }
}

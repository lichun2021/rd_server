package com.hawk.game.module.homeland.cfg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家园繁荣度属性
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "xml/homeland_prosperity_attr.xml")
public class HomeLandProsperityAttrCfg extends HawkConfigBase {
    @Id
    protected final int id;
    protected final int needProsperity;
    protected final String attr;
    protected final String reward;
    private Map<Const.EffType, Integer> effectMap = new HashMap<>();
    protected List<ItemInfo> rewardItem;

    public HomeLandProsperityAttrCfg() {
        id = 0;
        needProsperity = 0;
        attr = "";
        reward = "";
    }

    @Override
    protected boolean assemble() {
        // 初始化作用号的影响
        Map<Const.EffType, Integer> effectMapTemp = new HashMap<>();
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
                effectMapTemp.put(Const.EffType.valueOf(Integer.parseInt(idVal[0])), Integer.parseInt(idVal[1]));
            }
        }
        this.effectMap = ImmutableMap.copyOf(effectMapTemp);
        rewardItem = ImmutableList.copyOf(ItemInfo.valueListOf(reward));
        return true;
    }

    @Override
    protected boolean checkValid() {
        return true;
    }

    public int getNeedProsperity() {
        return needProsperity;
    }

    public int getId() {
        return id;
    }

    public Map<Const.EffType, Integer> getEffectMap() {
        return effectMap;
    }

    public List<ItemInfo> getRewardItem() {
        return rewardItem;
    }
}

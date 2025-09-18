package com.hawk.activity.type.impl.deepTreasure.cfg;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.List;

/**
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "activity/hidden_treasure_new/hidden_treasure_buff_new.xml")
public class DeepTreasureBuffCfg extends HawkConfigBase {
    /**
     * 成就id
     */
    @Id
    private final int buffId;
    private final int buffType;
    private final String buffPram;
    private List<Integer> buffPramList;

    public DeepTreasureBuffCfg() {
        buffId = 0;
        buffType = 0;
        buffPram = "2,500";
    }

    @Override
    protected boolean assemble() {
        buffPramList = SerializeHelper.stringToList(Integer.class, buffPram, SerializeHelper.BETWEEN_ITEMS);
        return true;
    }

    public int getBuffId() {
        return buffId;
    }

    public int getBuffType() {
        return buffType;
    }

    public String getBuffPram() {
        return buffPram;
    }

    public int getTimes() {
        if (!buffPramList.isEmpty()) {
            return buffPramList.get(0);
        }
        return 0;
    }

    public int getParam() {
        if (buffPramList.size() >= 2) {
            return buffPramList.get(1);
        }
        return 0;
    }
}

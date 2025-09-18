package com.hawk.game.config;

import com.hawk.game.item.ItemInfo;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

@HawkConfigManager.XmlResource(file = "xml/xhjz_reward.xml")
public class XHJZRewardCfg extends HawkConfigBase {
    @Id
    private final int id;
    private final int type;
    private final String lose;
    private final String reward;

    private List<ItemInfo> rewardItems;

    public XHJZRewardCfg(){
        id = 0;
        type = 0;
        lose = "";
        reward = "";
    }

    @Override
    protected boolean assemble() {
        try {
            this.rewardItems = ItemInfo.valueListOf(this.reward);
            return true;
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
    }

    public List<ItemInfo> getRewardItems() {
        return rewardItems;
    }
}

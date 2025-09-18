package com.hawk.game.module.dayazhizhan.playerteam.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 赛博之战商店配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/dyzz_war_reward.xml")
public class DYZZRewardCfg extends HawkConfigBase {
    @Id
    private final int id;
    private final int type;
    private final String reward;
    
    private List<ItemInfo> rewardItems;

    public DYZZRewardCfg() {
        id = 0;
        type = 0;
        reward = "";
    }

    public int getId() {
        return id;
    }

	public int getType() {
		return type;
	}
	
	
	public List<ItemInfo> getRewardItem() {
		List<ItemInfo> ret = new ArrayList<>();
		for (ItemInfo item : rewardItems) {
			ret.add(item.clone());
		}
		return ret;
	}
	
	@Override
    protected boolean assemble() {
		rewardItems = ItemInfo.valueListOf(reward);
        return true;
    }

    @Override
    protected boolean checkValid() {
        return true;
    }
    
   

	public List<ItemInfo> getRewardItems() {
    	if (rewardItems == null) {
    		return Collections.emptyList();
    	}
    	
    	return rewardItems.stream().map(ItemInfo::clone).collect(Collectors.toList());
    }
    
    

}

package com.hawk.activity.type.impl.shareprosperity.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/share_prosperity/%s/share_prosperity_reward.xml", autoLoad=false, loadParams="376")
public class ShareProsperityRewardCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final String payId;
    private final int type;
    private final String reward;
    private final int specialServer;
    
    private static Map<String, ShareProsperityRewardCfg> payIdCfgMap = new HashMap<>();
    private static Map<String, ShareProsperityRewardCfg> specialServerPayIdCfgMap = new HashMap<>();

    public ShareProsperityRewardCfg(){
        this.id = 0;
        this.payId = "";
        this.type = 0;
        this.reward = "";
        this.specialServer = 0;
    }
    
    @Override
    protected boolean assemble() {
    	if (specialServer > 0) {
    		specialServerPayIdCfgMap.put(payId, this);
    	} else {
    		payIdCfgMap.put(payId, this);
    	}
    	return true;
    }

	public int getId() {
		return id;
	}

	public String getPayId() {
		return payId;
	}

	public int getType() {
		return type;
	}

	public String getReward() {
		return reward;
	}

	public int getSpecialServer() {
		return specialServer;
	}

	public static ShareProsperityRewardCfg getConfig(String payId, boolean specialServer) {
		if (specialServer) {
			return specialServerPayIdCfgMap.get(payId);
		}
		return payIdCfgMap.get(payId);
	}
	
}
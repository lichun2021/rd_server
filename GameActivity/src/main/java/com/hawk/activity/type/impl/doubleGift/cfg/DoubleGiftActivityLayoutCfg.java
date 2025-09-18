package com.hawk.activity.type.impl.doubleGift.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

/**
 * 双享豪礼活动奖励分组配置
 * 
 */
@HawkConfigManager.XmlResource(file = "activity/double_gift/double_gift_choose.xml")
public class DoubleGiftActivityLayoutCfg extends HawkConfigBase {
	
	@Id
	private final int id; 
	
	private final int giftId;
	
	private final String reward;
	//默认
	private final int defaultChoose;
	
	// 礼包id与对应的可选奖励的映射
	private static Map<Integer, List<Integer>> giftRewardMap = new HashMap<>();
	
	public DoubleGiftActivityLayoutCfg(){
		this.id = 0;
		this.giftId = 0;
		this.reward = "";
		this.defaultChoose = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			if (giftRewardMap.containsKey(this.giftId)) {
				giftRewardMap.get(this.giftId).add(this.id);
			}else {
				List<Integer> idList = new ArrayList<>();
				idList.add(this.id);
				giftRewardMap.put(this.giftId, idList);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public static Map<Integer, List<Integer>> getGiftRewardMap() {
		return giftRewardMap;
	}

	public int getId() {
		return id;
	}

	public int getGiftId() {
		return giftId;
	}

	public String getReward() {
		return reward;
	}

	public int getDefaultChoose() {
		return defaultChoose;
	}
	//是否是默认
	public boolean isDefaultChoose() {
		return defaultChoose == 1;
	}
}

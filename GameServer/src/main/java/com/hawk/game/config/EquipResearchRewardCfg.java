package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;

/**
 * 装备研究奖励配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/equip_research_reward.xml")
public class EquipResearchRewardCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	/**
	 * 解锁条件
	 */
	protected final String unlock;
	
	/**
	 * 奖励
	 */
	protected final String rewards;

	/**
	 * 作用号触发
	 */
	protected final String effTouch;
	
	
	private int unlockResearchId;
	
	private int unlockResearchLevel;

	/**
	 * 触发作用号列表
	 */
	private List<EffectObject> effTouchList;
	
	/**
	 * 构造 
	 */
	public EquipResearchRewardCfg() {
		id = 0;
		unlock = "";
		rewards = "";
		effTouch = "";
	}


	public int getId() {
		return id;
	}

	public String getUnlock() {
		return unlock;
	}

	public String getRewards() {
		return rewards;
	}
	
	public List<EffectObject> getEffTouchList() {
		return effTouchList;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(unlock)) {
			unlockResearchId = Integer.parseInt(unlock.split("_")[0]);
			unlockResearchLevel = Integer.parseInt(unlock.split("_")[1]);
		}
		
		List<EffectObject> effTouchList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(rewards)) {
			String[] array = rewards.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				EffectObject effect = new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
				effTouchList.add(effect);
			}
		}
		this.effTouchList = effTouchList;
		
		return true;
	}

	public int getUnlockResearchId() {
		return unlockResearchId;
	}

	public int getUnlockResearchLevel() {
		return unlockResearchLevel;
	}
}

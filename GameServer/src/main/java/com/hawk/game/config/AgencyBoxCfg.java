package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 情报交易所宝箱配置
 * 
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/agency_box.xml")
public class AgencyBoxCfg extends HawkConfigBase {

	/**
	 * 情报等级
	 */
	@Id
	private final int boxId;
	
	/**
	 * 获取的等级
	 */
	private final int gotLevel;
	
	/**
	 * 解锁的等级 
	 */
	private final int unlockLevel;
	
	/**
	 * 奖励
	 */
	private final String reward;
	
	
	public AgencyBoxCfg() {
		boxId = 0;
		gotLevel = 0;
		unlockLevel = 0;
		reward = "";
	}

	public int getBoxId() {
		return boxId;
	}

	public int getGotLevel() {
		return gotLevel;
	}

	public int getUnlockLevel() {
		return unlockLevel;
	}

	public List<ItemInfo> getReward() {
		return ItemInfo.valueListOf(reward);
	}
}

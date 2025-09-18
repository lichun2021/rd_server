package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 情报交易所事件配置
 * 
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/agency_event.xml")
public class AgencyEventCfg extends HawkConfigBase {

	@Id
	private final int id;
	
	/**
	 * 事件类型
	 */
	private final int type;
	
	/**
	 * 事件品质
	 */
	private final int quality;
	
	/**
	 * 行为奖励
	 */
	private final String actionReward;
	
	/**
	 * 事件奖励
	 */
	private final String eventReward;
	
	
	/**
	 * 难度
	 */
	private final int difficulty;
	
	/**
	 * 是否是道具事件
	 */
	private final int itemEvent;
	
	/**
	 * 是否是升级事件
	 */
	private final int levelUpEvent;
	
	/**
	 * 是否是特殊事件
	 */
	private final int specialEvent;
	
	/**
	 * 特殊事件id
	 */
	private final int  specialId;
	
	/**
	 * 大本等级限制
	 */
	private final int cityLevel;
	
	/**
	 * 怪物最大等级
	 */
	private final int monsterLevel;
	
	/**
	 * 构造
	 */
	public AgencyEventCfg() {
		id = 0;
		type = 0;
		quality = 0;
		actionReward = "";
		eventReward = "";
		difficulty = 0;
		itemEvent = 0;
		levelUpEvent = 0;
		specialEvent = 0;
		specialId = 0;
		cityLevel = 0;
		monsterLevel = 1;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getQuality() {
		return quality;
	}


	public List<ItemInfo> getActionReward() {
		return ItemInfo.valueListOf(actionReward);
	}

	public List<ItemInfo> getEventReward() {
		return ItemInfo.valueListOf(eventReward);
	}


	public int getDifficulty() {
		return difficulty;
	}

	public int getItemEvent() {
		return itemEvent;
	}

	public int getSpecialEvent() {
		return specialEvent;
	}

	public int getLevelUpEvent() {
		return levelUpEvent;
	}

	public int getSpecialId() {
		return specialId;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public int getMonsterLevel() {
		return monsterLevel;
	}
	
	
}

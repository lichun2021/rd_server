package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.util.WeightAble;

/**
 * 国家任务配置表
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/nation_mission_task.xml")
public class NationMissionTaskCfg extends HawkConfigBase implements WeightAble {
	
	/**
	 * 任务id
	 */
	@Id
	private final int missionId;
	
	/**
	 * 任务等级
	 */
	private final int missionLevel;
	
	/**
	 * 任务类型
	 */
	private final int type;
	
	/**
	 * 条价值1
	 */
	private final String val1;
	
	/**
	 * 条件值2
	 */
	private final int val2;
	
	/**
	 * 权重
	 */
	private final int weight;
	
	/**
	 * 品质
	 */
	private final int taskQuality;
	
	/**
	 * 可接取次数
	 */
	private final int pickupTime;
	
	/**
	 * 科技奖励
	 */
	private final int nationalTechAward;
	
	/**
	 * 国家奖励
	 */
	private final String nationalAward;
	
	/**
	 * 个人奖励
	 */
	private final String personalAward;
	
	/**
	 * 完成时限
	 */
	private final int finishTime;
	
	/**
	 * 时间限制
	 */
	private final int timeLimit;
	
	private List<ItemInfo> nationalAwardItems;
	private List<ItemInfo> personalAwardItems;
	
	public NationMissionTaskCfg() {
		missionId = 0;
		missionLevel = 0;
		type = 0;
		val1 = "";
		val2 = 0;
		pickupTime = 0;
		taskQuality = 0;
		nationalTechAward = 0;
		nationalAward = "";
		personalAward = "";
		timeLimit = 0;
		weight = 0;
		finishTime = 0;
	}

	public int getMissionId() {
		return missionId;
	}

	public int getMissionLevel() {
		return missionLevel;
	}

	public int getType() {
		return type;
	}

	public String getVal1() {
		return val1;
	}

	public int getVal2() {
		return val2;
	}

	public int getWeight() {
		return weight;
	}

	public int getTaskQuality() {
		return taskQuality;
	}

	public int getPickupTime() {
		return pickupTime;
	}

	public int getNationalTechAward() {
		return nationalTechAward;
	}

	public String getNationalAward() {
		return nationalAward;
	}

	public String getPersonalAward() {
		return personalAward;
	}

	public long getTimeLimit() {
		return timeLimit * 1000L;
	}

	public long getFinishTime() {
		return finishTime * 1000L;
	}
	
	public List<ItemInfo> getNationalAwardItems() {
		return new ArrayList<>(nationalAwardItems);
	}

	public List<ItemInfo> getPersonalAwardItems() {
		return new ArrayList<>(personalAwardItems);
	}

	public MissionCfgItem getMissionCfgItem() {
		return new MissionCfgItem(missionId, type, val1, val2);
	}
	
	@Override
	protected boolean assemble() {
		nationalAwardItems = ItemInfo.valueListOf(nationalAward);
		personalAwardItems = ItemInfo.valueListOf(personalAward);
		return true;
	}
}

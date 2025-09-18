package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.service.mssion.MissionType;

/**
 * 玩家成就配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/achievement.xml")
public class PlayerAchieveCfg extends HawkConfigBase {
	/**
	 * 配置id
	 */
	@Id
	private final int id;
	/**
	 * 成就组id
	 */
	private final int groupId;
	/**
	 * 成就(任务)类型
	 */
	private final int achieveType;
	/**
	 * 等级
	 */
	private final int level;
	/**
	 * 条件值
	 */
	private final String condition;
	/**
	 * 目标
	 */
	private final int target;
	/**
	 * 奖励
	 */
	private final String reward;

	/**
	 * 是否唯一
	 */
	private final int isRare;
	
	
	/**
	 * 完成此成就获得多少成就点
	 */
	private final int star;
	
	
	private List<ItemInfo> rewards;
	
	public PlayerAchieveCfg() {
		id = 0;
		groupId = 0;
		achieveType = 0;
		level = 0;
		condition = "";
		target = 0;
		reward = "";
		isRare = 0;
		star = 0;
	}

	public int getId() {
		return id;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getAchieveType() {
		return achieveType;
	}

	public int getLevel() {
		return level;
	}

	public String getCondition() {
		return condition;
	}

	public int getTarget() {
		return target;
	}

	public String getReward() {
		return reward;
	}
	
	public boolean isSole() {
		return isRare > 0;
	}

	public int getStar(){
		return star;
	}
	
	public List<ItemInfo> getRewards() {
		return rewards;
	}

	public MissionCfgItem getMissionCfgItem() {
		return new MissionCfgItem(id, achieveType, condition, target);
	}
	
	@Override
	protected boolean assemble() {
		List<ItemInfo> rewards = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(reward)) {
			rewards = ItemInfo.valueListOf(reward);
		}
		this.rewards = rewards;
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		if (MissionType.valueOf(achieveType) == null) {
			HawkLog.errPrintln("achieve cfg check error, type:{}", achieveType);
			return false;
		}
		return true;
	}
}

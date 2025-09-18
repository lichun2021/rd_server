package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.service.mssion.MissionType;

/**
 * 剧情任务配置
 * 
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/dramaTask.xml")
public class StoryMissionCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 章节id
	 */
	private final int chapter;
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
	 * 条件值2
	 */
	private final int val3;
	/**
	 * 奖励
	 */
	private final String reward;

	public StoryMissionCfg() {
		id = 0;
		chapter = 0;
		type = 0;
		val1 = "";
		val2 = 0;
		val3 = 0;
		reward = "";
	}

	public int getId() {
		return id;
	}

	public int getChapter() {
		return chapter;
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
	
	public int getVal3() {
		return val3;
	}

	public String getReward() {
		return reward;
	}

	public List<ItemInfo> getRewardItem() {
		return ItemInfo.valueListOf(reward);
	}

	public MissionCfgItem getMissionCfgItem() {
		return new MissionCfgItem(id, type, val1, val2);
	}
	
	@Override
	protected boolean checkValid() {
		if (MissionType.valueOf(type) == null) {
			HawkLog.errPrintln("story mission cfg check error, type:{}", type);
			return false;
		}
		return true;
	}
}

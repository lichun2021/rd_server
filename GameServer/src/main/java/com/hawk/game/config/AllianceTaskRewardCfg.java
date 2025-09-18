package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 联盟任务奖励配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_task_reward.xml")
public class AllianceTaskRewardCfg extends HawkConfigBase {
	@Id
	/**
	 * id
	 */
	protected final int id;
	/**
	 * 任务id
	 */
	private final int taskId;
	/**
	 * 任务类型
	 */
	private final int cityLvlMin;
	/**
	 * 任务类型
	 */
	private final int cityLvlMax;
	/**
	 * 任务奖励
	 */
	private final String rewards;
	/**
	 * 任务奖励列表
	 */
	private List<ItemInfo> rewardItems;
	

	public AllianceTaskRewardCfg() {
		this.id = 0;
		this.taskId = 0;
		this.cityLvlMin = 0;
		this.cityLvlMax = 0;
		this.rewards = "";
	}

	public int getId() {
		return id;
	}
	
	public void setRewardItems(List<ItemInfo> rewardItems) {
		this.rewardItems = rewardItems;
	}

	public int getTaskId() {
		return taskId;
	}

	public int getCityLvlMin() {
		return cityLvlMin;
	}

	public int getCityLvlMax() {
		return cityLvlMax;
	}

	public String getRewards() {
		return rewards;
	}

	public List<ItemInfo> getRewardItem() {
		List<ItemInfo> copy = new ArrayList<>();
		for(ItemInfo item : rewardItems){
			copy.add(item.clone());
		}
		return copy;
	}

	@Override
	protected boolean assemble() {
		this.rewardItems = ItemInfo.valueListOf(this.rewards);
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		AllianceTaskCfg taskCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceTaskCfg.class, this.taskId);
		if(taskCfg == null){
			return false;
		}
		return super.checkValid();
	}
	
	

}

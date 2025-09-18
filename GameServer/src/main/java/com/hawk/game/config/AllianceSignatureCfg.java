package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 联盟签到配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_signature.xml")
public class AllianceSignatureCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	private final int dayMin;
	
	private final int dayMax;
	
	/**
	 * 任务奖励
	 */
	private final String rewards;
	
	/**
	 * 任务奖励列表
	 */
	private List<ItemInfo> rewardItems;

	public AllianceSignatureCfg() {
		this.id = 0;
		this.dayMin = 0;
		this.dayMax = 0;
		this.rewards = "";
	}

	public List<ItemInfo> getRewardItems() {
		List<ItemInfo> copy = new ArrayList<>();
		for(ItemInfo item : rewardItems){
			copy.add(item.clone());
		}
		return copy;
	}

	public int getId() {
		return id;
	}

	public int getDayMin() {
		return dayMin;
	}

	public int getDayMax() {
		return dayMax;
	}

	public String getRewards() {
		return rewards;
	}

	@Override
	protected boolean assemble() {
		this.rewardItems = ItemInfo.valueListOf(this.rewards);
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		// TODO
		return super.checkValid();
	}
	
}

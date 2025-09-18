package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 跨服任务配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_pack.xml")
public class CrossPackCfg extends HawkConfigBase {
	@Id
	/**
	 * 礼包id
	 */
	protected final int id;
	/**
	 * 礼包类型
	 */
	private final int type;
	/**
	 * 礼包发放类型,同类型礼包不能重复颁发给一个人
	 */
	private final int sendType;
	/**
	 * 任务奖励
	 */
	private final String reward;

	/**
	 * 礼包名字
	 */
	private final String giftName;
	
	/**
	 * 礼包数量
	 */
	private final int  num;
	
	/**
	 * 任务奖励列表
	 */
	private List<ItemInfo> rewardItems;
	
	public CrossPackCfg() {
		this.id = 0;
		this.type = 0;
		this.reward = "";
		this.num = 0;
		this.giftName = "";
		this.sendType = 0;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getNum() {
		return num;
	}

	public void setRewardItems(List<ItemInfo> rewardItems) {
		this.rewardItems = rewardItems;
	}

	public String getGiftName() {
		return giftName;
	}

	public int getSendType() {
		return sendType;
	}

	public List<ItemInfo> getRewardItems() {
		List<ItemInfo> copy = new ArrayList<>();
		for(ItemInfo item : rewardItems){
			copy.add(item.clone());
		}
		return copy;
	}

	@Override
	protected boolean assemble() {
		this.rewardItems = ItemInfo.valueListOf(this.reward);
		return super.assemble();
	}

}

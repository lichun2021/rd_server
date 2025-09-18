package com.hawk.activity.type.impl.groupBuy.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 最高折扣的奖励表
 * 
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "activity/group_buy/group_top_discount_rewards.xml")
public class GroupTopDiscountCfg extends HawkConfigBase {
	@Id
	private final int id;
	//购买次数
	private final String buyTimes;
	//奖励
	private final String rewards;
	//最高折扣免费积分
	private final int points;
	
	private List<Integer> buyTimesList;
	private List<String> buyTimesRewardList;
	
	public GroupTopDiscountCfg() {
		this.id = 0;
		this.buyTimes = "";
		this.rewards = "";
		this.points = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			buyTimesList = SerializeHelper.stringToList(Integer.class, buyTimes, "_");
			buyTimesRewardList = SerializeHelper.stringToList(String.class, rewards, ",");
			if (buyTimesList.size() != buyTimesRewardList.size()) {
				HawkLog.errPrintln("activity/group_buy/group_top_discount_rewards.xml, buyTimes not match rewards count, id: {}", id);
				return false;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public String getRewards() {
		return rewards;
	}

	public int getPoints() {
		return points;
	}

	public List<Integer> getBuyTimesList() {
		return buyTimesList;
	}
	
	public List<String> getBuyTimesRewardList() {
		return buyTimesRewardList;
	}
}

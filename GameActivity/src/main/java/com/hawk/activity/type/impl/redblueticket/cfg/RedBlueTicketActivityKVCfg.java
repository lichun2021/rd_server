package com.hawk.activity.type.impl.redblueticket.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 翻牌活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/red_blue_ticket/red_blue_cfg.xml")
public class RedBlueTicketActivityKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	
	private final int limittimes;
	
	private final String expend;
	
	private final String Aaconsume;
	
	private final String Areward;
	
	private final String Baconsume;
	
	private final String Breward;
	
	private final String Ticketnum;
	
	List<RewardItem.Builder> consumeItemAList;
	List<RewardItem.Builder> consumeItemBList;
	List<RewardItem.Builder> rewardItemAList;
	List<RewardItem.Builder> rewardItemBList;
	List<RewardItem.Builder> expendItemList;
	
	private int ticketIdMin = 1;
	private int ticketIdMax = 8;

	public RedBlueTicketActivityKVCfg() {
		serverDelay = 0;
		limittimes = 0;
		expend = "";
		Aaconsume = "";
		Areward = "";
		Baconsume = "";
		Breward = "";
		Ticketnum = "";
	}

	public String getExpend() {
		return expend;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public int getLimittimes() {
		return limittimes;
	}
	
	public boolean assemble() {
		consumeItemAList = RewardHelper.toRewardItemImmutableList(Aaconsume);
		rewardItemAList = RewardHelper.toRewardItemImmutableList(Areward);
		consumeItemBList = RewardHelper.toRewardItemImmutableList(Baconsume);
		rewardItemBList = RewardHelper.toRewardItemImmutableList(Breward);
		expendItemList = RewardHelper.toRewardItemImmutableList(expend);
		if (!HawkOSOperator.isEmptyString(Ticketnum)) {
			String[] nums = Ticketnum.split(",");
			ticketIdMin = Integer.parseInt(nums[0]);
			ticketIdMax = Integer.parseInt(nums[1]);
			if (ticketIdMin >= ticketIdMax) {
				return false;
			}
		}
		return super.assemble();
	}

	public List<RewardItem.Builder> getConsumeItemAList() {
		return consumeItemAList;
	}

	public List<RewardItem.Builder> getConsumeItemBList() {
		return consumeItemBList;
	}

	public List<RewardItem.Builder> getRewardItemAList() {
		return rewardItemAList;
	}

	public List<RewardItem.Builder> getRewardItemBList() {
		return rewardItemBList;
	}
	
	public List<RewardItem.Builder> getExpendItemList() {
		return expendItemList;
	}

	public int getTicketIdMin() {
		return ticketIdMin;
	}

	public int getTicketIdMax() {
		return ticketIdMax;
	}

}

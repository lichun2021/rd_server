package com.hawk.activity.type.impl.lotteryTicket.config;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 *  中部养成计划
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/lottery_ticket/lottery_ticket_cfg.xml")
public class LotteryTicketKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	// 彩票道具消耗
	private final String lotteryCost;
	//多少次更换权重
	private final int randomWeightLimit;
	//彩票上面抽奖位置个数
	private final int lotteryPosCount;
	//代刮限制次数
	private final int assistLimit;
	//代刮时间限制 秒
	private final int assistOutTime;
	//活动结束前多长时间不可以代刮
	private final int assistTimeLimit;
	//回收道具
	private final String recoverItem;
	private final String recoverItemGain;
	//弹幕个数
	private final int barrageSize;
	
	private final String extReward;
	
	private final int randomFrom;
	private final int randomTo;
	
	
	
	
	public LotteryTicketKVCfg() {
		serverDelay = 0;
		lotteryCost = "";
		randomWeightLimit = 0;
		lotteryPosCount= 0;
		assistLimit = 0;
		assistOutTime = 0;
		assistTimeLimit = 0;
		recoverItem = "";
		recoverItemGain = "";
		barrageSize = 0;
		extReward= "";
		randomFrom= 0;
		randomTo = 0;
	}
	
	
	@Override
	protected boolean assemble() {
		return true;
	}


	public long getServerDelay() {
		return serverDelay  * 1000l;
	}

	
	public int getRandomWeightLimit() {
		return randomWeightLimit;
	}
	
	public int getLotteryPosCount() {
		return lotteryPosCount;
	}
	
	
	public int getAssistLimit() {
		return assistLimit;
	}
	
	public int getAssistOutTime() {
		return assistOutTime;
	}
	
	public int getAssistTimeLimit() {
		return assistTimeLimit;
	}
	
	
	public List<RewardItem.Builder> getLotteryCostItems() {
		return RewardHelper.toRewardItemImmutableList(this.lotteryCost);
	}
	
	
	public String getRecoverItem() {
		return recoverItem;
	}
	
	public String getRecoverItemGain() {
		return recoverItemGain;
	}
	
	public int getBarrageSize() {
		return barrageSize;
	}
	
	public String getExtReward() {
		return extReward;
	}

	public int getRandomFrom() {
		return randomFrom;
	}
	
	public int getRandomTo() {
		return randomTo;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.lotteryCost);
		if (!valid) {
			throw new InvalidParameterException(String.format("KVResource reward error, id: %s , needItem: %s", 1, lotteryCost));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.recoverItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("KVResource recoverItem error, id: %s , needItem: %s", 1, recoverItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.recoverItemGain);
		if (!valid) {
			throw new InvalidParameterException(String.format("KVResource recoverItemGain error, id: %s , needItem: %s", 1, recoverItemGain));
		}
		
		return super.checkValid();
	}
	

	
}
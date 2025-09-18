package com.hawk.activity.type.impl.dividegold.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**金币瓜分(集福)活动 
 * @author Winder
 */
@HawkConfigManager.KVResource(file = "activity/divide_gold/dividegold_cfg.xml")
public class DivideGoldActivityKVCfg extends HawkConfigBase {

	/** 服务器开服延时开启活动时间 */
	private final int serverDelay;

	/** 是否每日重置(零点跨天重置) */
	private final int isDailyReset;

	private final int getCD;// = 3600
	//自动刷新时间
	private final String freshTime;
	//开启道具字宝箱消耗
	private final String chestCost;
	//合成红包消耗
	private final String redEnvelopeCost;
	//跑马灯发送条件
	private final int noticeCondition;
	//红包道具
	private final String redEnvelope;
	//合成红包次数限制
	private final int redEnvelopeLimit;
	
	// 注水
	private final int Act150Pram_M;
	private final int Act150Pram_a;
	private final int Act150Pram_b;
	private final int Act150Pram_c;
	private final int Act150Pram_d;
	
	//微信注水系数
	private final int expandRate_wx; 
	private final int maxLimit_wx;
	private final int reduceRate_wx;
	//手Q注水系数
	private final int expandRate_qq;
	private final int maxLimit_qq;
	private final int reduceRate_qq;
	
	
	protected int[] refreshTime;
	//开启道具字宝箱消耗2
	private List<RewardItem.Builder> chestConsume;
	//合成红包消耗2
	private List<RewardItem.Builder> redEnvelopeConsume;
	
	//红包道具
	private List<RewardItem.Builder> redEnvelopeReward;
	//每日索要成功次数限制
	private final int dailyAskFor;
	// 每日赠送限制
	private final int dailyGive;
	
	public DivideGoldActivityKVCfg() {
		serverDelay = 0;
		isDailyReset = 0;
		getCD = 3600;
		freshTime = "";
		chestCost = "";
		redEnvelopeCost = "";
		noticeCondition = 0;
		redEnvelope = "";
		redEnvelopeLimit = 0;
		dailyAskFor = 0;
		dailyGive = 0;
		Act150Pram_M = 0;
		Act150Pram_a = 0;
		Act150Pram_b = 0;
		Act150Pram_c = 0;
		Act150Pram_d = 0;
		expandRate_wx = 0; 
		maxLimit_wx = 0;
		reduceRate_wx = 0;
		expandRate_qq = 0;
		maxLimit_qq = 0;
		reduceRate_qq = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public boolean isDailyReset() {
		return isDailyReset == 1;
	}

	public int getGetCD() {
		return getCD;
	}

	public String getFreshTime() {
		return freshTime;
	}

	public int[] getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(int[] refreshTime) {
		this.refreshTime = refreshTime;
	}

	public String getChestCost() {
		return chestCost;
	}

	public String getRedEnvelopeCost() {
		return redEnvelopeCost;
	}

	public List<RewardItem.Builder> getChestConsume() {
		return chestConsume;
	}


	public int getIsDailyReset() {
		return isDailyReset;
	}

	public List<RewardItem.Builder> getRedEnvelopeConsume() {
		return redEnvelopeConsume;
	}

	public int getNoticeCondition() {
		return noticeCondition;
	}

	public List<RewardItem.Builder> getRedEnvelopeReward() {
		return redEnvelopeReward;
	}

	public void setRedEnvelopeReward(List<RewardItem.Builder> redEnvelopeReward) {
		this.redEnvelopeReward = redEnvelopeReward;
	}

	public String getRedEnvelope() {
		return redEnvelope;
	}

	public int getRedEnvelopeLimit() {
		return redEnvelopeLimit;
	}

	public int getDailyAskFor() {
		return dailyAskFor;
	}

	public int getDailyGive() {
		return dailyGive;
	}
	

	public int getAct150Pram_M() {
		return Act150Pram_M;
	}

	public double getAct150Pram_a() {
		return Act150Pram_a/10000d;
	}

	public int getAct150Pram_b() {
		return Act150Pram_b;
	}

	public double getAct150Pram_c() {
		return Act150Pram_c/10000d;
	}

	public int getAct150Pram_d() {
		return Act150Pram_d;
	}
	

	public int getExpandRate_wx() {
		return expandRate_wx;
	}

	public int getMaxLimit_wx() {
		return maxLimit_wx;
	}

	public int getReduceRate_wx() {
		return reduceRate_wx;
	}

	public int getExpandRate_qq() {
		return expandRate_qq;
	}

	public int getMaxLimit_qq() {
		return maxLimit_qq;
	}

	public int getReduceRate_qq() {
		return reduceRate_qq;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(freshTime)) {
			String[] timeArray = freshTime.split("_");
			int[] intTimeArray = new int[timeArray.length];			
			for (int i = 0; i < timeArray.length; i++) {
				intTimeArray[i] =  Integer.parseInt(timeArray[i]);
			}
			refreshTime = intTimeArray;
			
			chestConsume = RewardHelper.toRewardItemImmutableList(chestCost);
			redEnvelopeConsume = RewardHelper.toRewardItemImmutableList(redEnvelopeCost);
			redEnvelopeReward = RewardHelper.toRewardItemImmutableList(redEnvelope);
			
		} else {
			throw new RuntimeException("DivideGoldActivityKVCfg refresh time must not null or empty");
		}
		return true;
	}

}
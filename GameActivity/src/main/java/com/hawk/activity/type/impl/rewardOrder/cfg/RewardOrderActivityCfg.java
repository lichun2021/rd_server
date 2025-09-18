package com.hawk.activity.type.impl.rewardOrder.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/reward_order/rewardOrderActivityCfg.xml")
public class RewardOrderActivityCfg extends HawkConfigBase {
	
	private static RewardOrderActivityCfg instance;
	
	public static RewardOrderActivityCfg getInstance(){
		return instance;
	}
	
	/** 服务器开服延时开启活动时间 **/
	private final long serverDelay;
	
	/** 悬赏令刷新时间 **/
	private final int freshTime;
	
	/** 手动刷新消耗（type_itemId_count,type_itemId_count） **/
	private final String freshCost;
	
	/** 每日刷新价格是否重置 **/
	private final int reset;
	
	/** 每日最大完成次数 **/
	private final int maxFinishCount;
	
	public RewardOrderActivityCfg(){
		instance = this;
		freshTime = 0;
		freshCost = "";
		reset = 0;
		serverDelay = 0;
		maxFinishCount = 0;
	}

	public int getFreshTime() {
		return freshTime;
	}

	public String getFreshCost() {
		return freshCost;
	}

	public int getReset() {
		return reset;
	}
	
	public boolean isReset(){
		return reset == 1;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getMaxFinishCount() {
		return maxFinishCount;
	}

	@Override
	protected boolean checkValid() {
		boolean result = ConfigChecker.getDefaultChecker().checkAwardsValid(freshCost);
		if(!result){
			logger.error("刷新消耗配置错误.");
			return false;
		}
		if(maxFinishCount <= 0){
			logger.error("每日完成上限配置异常:" + maxFinishCount);
			return false;
		}
		return super.checkValid();
	}

	/***
	 * 获取刷新消耗
	 * @param freshCount 刷新次数，从1开始
	 * @return
	 */
	public RewardItem.Builder getPlayerFreshCost(int freshCount){
		String src[] = freshCost.split(",");
		String chose = null;
		int index = -1;
		if(freshCount >= src.length){
			//取数组最后一位
			index = src.length - 1;
		}else{
			index = freshCount - 1;
		}
		chose = src[index];
		RewardItem.Builder build = RewardItem.newBuilder();
		String s[] = chose.split(SerializeHelper.ATTRIBUTE_SPLIT);
		int type = Integer.parseInt(s[0]);
		int itemId = Integer.parseInt(s[1]);
		int count = Integer.parseInt(s[2]);
		build.setItemType(type);
		build.setItemId(itemId);
		build.setItemCount(count);
		return build;
	}
}

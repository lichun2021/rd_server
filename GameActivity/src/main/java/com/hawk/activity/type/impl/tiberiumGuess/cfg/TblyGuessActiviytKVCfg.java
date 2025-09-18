package com.hawk.activity.type.impl.tiberiumGuess.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**泰伯利亚联赛竞猜活动 配置
 * @author Winder
 *
 */
@HawkConfigManager.KVResource(file = "activity/tiberium_bet/tiberium_bet_activity_cfg.xml")
public class TblyGuessActiviytKVCfg extends HawkConfigBase {
	//服务器开服延时开启活动时间 
	private final int serverDelay;
	//投票消耗
	private final String betCost;
	//消耗
	private List<RewardItem.Builder> betConsume;
	//开始轮数
	private final int startRound;
	//结束轮数
	private final int endRound;
	
	public TblyGuessActiviytKVCfg(){
		serverDelay = 0;
		betCost = "";
		startRound = 0;
		endRound = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getBetCost() {
		return betCost;
	}
	
	
	public List<RewardItem.Builder> getBetConsume() {
		return betConsume;
	}

	public void setBetConsume(List<RewardItem.Builder> betConsume) {
		this.betConsume = betConsume;
	}
	
	public int getStartRound() {
		return startRound;
	}

	public int getEndRound() {
		return endRound;
	}

	@Override
	protected boolean assemble() {
		betConsume = RewardHelper.toRewardItemImmutableList(betCost);
		return true;
	}
	
}

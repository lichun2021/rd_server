package com.hawk.activity.type.impl.rewardOrder.cfg;

import java.security.InvalidParameterException;
import java.util.List;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/***
 * 悬赏令配置
 * @author yang.rao
 *
 */
@HawkConfigManager.XmlResource(file = "activity/reward_order/rewardOrderCfg.xml")
public class RewardOrderCfg extends AchieveConfig{
	
	/** 悬赏令品质 **/
	public static final int SSS = 1;
	public static final int SS = 2;
	public static final int S = 3;
	public static final int A = 4;
	public static final int B = 5;
	public static final int C = 6;
	
	@Id
	private final int id;
	
	/** 悬赏令名称 **/
	private final String name;
	
	/** 品质1~6 数字越小，质量越高 1为sss品质，6为c品质 **/
	private final int quality;
	
	/** 持续时间 **/
	private final long consistTime;
	
	/** 悬赏令刷出的概率 **/
	private final int rate;
	
	/** 悬赏令奖励 **/
	private final String reward;
	
	public RewardOrderCfg(){
		id = 0;
		name = "";
		quality = 0;
		consistTime = 0;
		rate = 0;
		reward = "";
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public int getQuality() {
		return quality;
	}

	public long getConsistTime() {
		return consistTime;
	}

	public int getRate() {
		return rate;
	}

	public String getReward() {
		return reward;
	}
	
	public List<RewardItem.Builder> getRewardList(){
		return RewardHelper.toRewardItemList(reward);
	}

	@Override
	protected boolean checkValid() {
		if(getQuality() > 6 || getQuality() < 1){
			throw new InvalidParameterException(String.format("quality error, id: %d, Class name: %s ", getId(), getClass().getName()));
		}
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(getReward());
		if (!valid) {
			throw new InvalidParameterException(String.format("achieve item reward error, achieveId: %d, Class name: %s ", getAchieveId(), getClass().getName()));
		}
		return true;
	}
	
	
}

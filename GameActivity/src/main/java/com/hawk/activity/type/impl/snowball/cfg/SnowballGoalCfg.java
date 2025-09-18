package com.hawk.activity.type.impl.snowball.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 雪球大战进球配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/snowball/snowball_goal.xml")
public class SnowballGoalCfg extends HawkConfigBase {
	
	@Id
	private final int id;

	/**
	 * 坐标 x,y
	 */
	private final String pos;
	
	/**
	 * 目标数量
	 */
	private final int target;

	/**
	 * 玩家进球数达到x的，发放奖励
	 */
	private final int playerRewardValue;

	/**
	 * 奖励(玩家进球数达到x的，发放奖励)
	 */
	private final String reward;

	/**
	 * 进球奖励
	 */
	private final int goalAward;
	
	/**
	 * 进球积分
	 */
	private final int goalScore;
	
	/**
	 * 是否显示
	 */
	private final boolean show;

	private final String goalReward;
	
	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> rewardList;
	
	/**
	 * 坐标x
	 */
	private int posX;
	
	/**
	 * 坐标y
	 */
	private int posY;
	
	/**
	 * 构造
	 */
	public SnowballGoalCfg() {
		id = 0;
		pos = "";
		target = 0;
		playerRewardValue = 0;
		reward = "";
		goalScore = 0;
		show = true;
		goalAward = 0;
		goalReward = "";
	}

	public String getGoalReward() {
		return goalReward;
	}

	public int getId() {
		return id;
	}

	public String getPos() {
		return pos;
	}

	public int getTarget() {
		return target;
	}

	public int getPlayerRewardValue() {
		return playerRewardValue;
	}

	public String getReward() {
		return reward;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public int getPointId() {
		return (posY << 16) | posX;
	}
	
	public int getGoalScore() {
		return goalScore;
	}

	public boolean isShow() {
		return show;
	}

	public int getGoalAward() {
		return goalAward;
	}

	@Override
	protected boolean assemble() {
		
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(reward)) {
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
		}
		this.rewardList = rewardList;
		
		if (!HawkOSOperator.isEmptyString(pos)) {
			String[] split = pos.split(",");
			posX = Integer.parseInt(split[0]);
			posY = Integer.parseInt(split[1]);
		}
		return true;
	}
}

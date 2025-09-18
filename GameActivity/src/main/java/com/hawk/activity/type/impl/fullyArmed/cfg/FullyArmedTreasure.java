package com.hawk.activity.type.impl.fullyArmed.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 寻宝配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/fully_armed/fully_armed_treasure.xml")
public class FullyArmedTreasure extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 奖池*/
	private final int precondition;
	/** 花费*/
	private final String cost;
	/**奖励列表有随机*/
	private final int awardId;
	
	private List<RewardItem.Builder> costList;
	
	private static int maxId = 0;
	public FullyArmedTreasure() {
		id = 0;
		cost = "";
		precondition = 0;
		awardId = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			costList = RewardHelper.toRewardItemImmutableList(cost);
			if(maxId < id){
				maxId = id;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(cost);
		if (!valid) {
			throw new InvalidParameterException(String.format("FullyArmedTreasure cost error, id: %s , cost: %s", id, cost));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getAwardId() {
		return awardId;
	}

	public int getPrecondition() {
		return precondition;
	}

	public String getCost() {
		return cost;
	}
	
	public static int getMaxId() {
		return maxId;
	}
	public List<RewardItem.Builder> getCostList() {
		return costList;
	}
}

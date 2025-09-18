package com.hawk.activity.type.impl.luckyBox.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * @author Richard
 */
@HawkConfigManager.XmlResource(file = "activity/lucky_box/lucky_box_turntable_reward.xml")
public class LuckyBoxTurntableRewardCfg extends HawkConfigBase implements HawkRandObj{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	/**
	 * 当前奖励是否可替换
	 */
	private final int canSelected;
	/**
	 * 奖励有多少份，每次抽中减1,等于0时表示当天条目已经抽完了
	 */
	private final int number;
	/**
	 * 从被抽光的格子可以继承的权重，多个格子，每个格子的此值和做分母，此值做分母，来分配可继承的权重
	 */
	private final int assignweight;
	/**
	 * 自身的初始权重
	 */
	private final int weight;
	/**
	 * 实际的奖励
	 */
	private final String rewardId;

	public LuckyBoxTurntableRewardCfg() {
		id = 0;
		canSelected = 0;
		number  = 0;
		assignweight = 0;
		weight = 0;
		rewardId = "";
	}

	public int getId() {
		return id;
	}

	public int getCanSelected() {
		return canSelected;
	}

	public int getNumber() {
		return number;
	}

	public int getAssignweight() {
		return assignweight;
	}

	public List<Integer> getRewardIdList() {
		return SerializeHelper.stringToList(Integer.class, this.rewardId, SerializeHelper.BETWEEN_ITEMS);
	}

	@Override
	public int getWeight() {
		return weight;
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewardId);
		if (!valid) {
			throw new InvalidParameterException(String.format(
					"LuckyBoxRewardCfg reward error, id: %s , needItem: %s", id, rewardId));
		}
		return super.checkValid();
	}
}

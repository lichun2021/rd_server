package com.hawk.activity.type.impl.honourHeroBefell.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/honour_hero_befell/honour_hero_befell_reward.xml")
public class HonourHeroBefellRewardCfg extends HawkConfigBase implements HawkRandObj{

	@Id
	private final int id;

	private final int weight;
	
	private final String reward;
	

	public HonourHeroBefellRewardCfg() {
		this.id = 0;
		this.weight = 0;
		this.reward = "";
	}
	
	public int getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}
	

	public List<RewardItem.Builder> getRewardItemList() {
		return RewardHelper.toRewardItemImmutableList(this.reward);
	}
	
	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("HonourHeroBefellRewardCfg reward error, id: %s , commonAward: %s", id, reward));
		}
		return super.checkValid();
	}

	
}

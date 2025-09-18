package com.hawk.activity.type.impl.yurirevenge.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 尤里复仇排行奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "activity/yuri_revenge/yuri_revenge_rank_reward.xml")
public class YuriRevengeRankReward extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	
	/** 个人排行奖励*/
	private final String personAward;
	
	/** */
	private final String allianceAward;
	
	private List<RewardItem.Builder> personAwardList;
	private List<RewardItem.Builder> allianceAwardList;
	
	public YuriRevengeRankReward() {
		id = 0;
		personAward = "";
		allianceAward = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			personAwardList = RewardHelper.toRewardItemImmutableList(personAward);
			allianceAwardList = RewardHelper.toRewardItemImmutableList(allianceAward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	
	public int getId() {
		return id;
	}

	public String getPersonAward() {
		return personAward;
	}

	public String getAllianceAward() {
		return allianceAward;
	}

	public List<RewardItem.Builder> getPersonAwardList() {
		return personAwardList;
	}
	
	public List<RewardItem.Builder> getAllianceAwardList() {
		return allianceAwardList;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(personAward);
		if (!valid) {
			throw new InvalidParameterException(String.format("YuriRevengeRankReward reward error, id: %s , personAward: %s", id, personAward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(allianceAward);
		if (!valid) {
			throw new InvalidParameterException(String.format("YuriRevengeRankReward reward error, id: %s , allianceAward: %s", id, allianceAward));
		}
		return super.checkValid();
	}

}

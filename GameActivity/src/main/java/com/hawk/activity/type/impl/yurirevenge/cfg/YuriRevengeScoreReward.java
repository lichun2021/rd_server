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
 * 尤里复仇积分奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "activity/yuri_revenge/yuri_revenge_score_reward.xml")
public class YuriRevengeScoreReward extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	
	/** 个人积分条件*/
	private final int personIntegral;

	/** 联盟积分条件*/
	private final int allianceIntegral;
	
	/** 奖励*/
	private final String item;
	
	private List<RewardItem.Builder> awardList;
	
	public YuriRevengeScoreReward() {
		id = 0;
		personIntegral = 0;
		allianceIntegral = 0;
		item = "";
	}
	
	public int getId() {
		return id;
	}

	public int getPersonIntegral() {
		return personIntegral;
	}

	public int getAllianceIntegral() {
		return allianceIntegral;
	}

	public String getItem() {
		return item;
	}

	public List<RewardItem.Builder> getAwardList() {
		return awardList;
	}

	@Override
	protected boolean assemble() {
		try {
			awardList = RewardHelper.toRewardItemImmutableList(item);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(item);
		if (!valid) {
			throw new InvalidParameterException(String.format("YuriRevengeScoreReward reward error, id: %s , item: %s", id, item));
		}
		return super.checkValid();
	}
}

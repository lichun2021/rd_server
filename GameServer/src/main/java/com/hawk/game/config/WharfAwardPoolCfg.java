package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 码头奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/wharf_award_pool.xml")
public class WharfAwardPoolCfg extends HawkConfigBase {
	/** id*/
	@Id
	protected final int id;
	/** 奖励*/
	protected final int awardId;
	/** 下一个序号*/
	protected final int ratio;
	/** 奖励内容*/
	protected final String award;
	
	public WharfAwardPoolCfg() {
		this.id = 0;
		this.awardId = 0;
		this.ratio = 0;
		this.award = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			RewardHelper.toRewardItemImmutableList(award);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	public int getId() {
		return id;
	}
	
	public int getAwardId() {
		return awardId;
	}

	public int getRate() {
		return ratio;
	}

	public List<RewardItem.Builder> getRewardList() {
		return RewardHelper.toRewardItemList(award);
	}

}

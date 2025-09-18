package com.hawk.activity.type.impl.alliancecelebrate.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.List;


/**
 * 双十一联盟欢庆 等级奖励配置
 * @author hf
 */
@HawkConfigManager.XmlResource(file = "activity/ssy_alliance_celebrate/ssy_alliance_celebrate_lv.xml")
public class AllianceCelebrateLevelCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int lv;
	//经验值
	private final int allianceExp;
	// 奖励内容
	private final String rewardPt1;
	//玩家限制贡献
	private final int personExpPt1;
	// 奖励内容2
	private final String rewardPt2;
	//玩家限制贡献2
	private final int personExpPt2;

	private List<Reward.RewardItem.Builder> awardItem1;

	private List<Reward.RewardItem.Builder> awardItem2;

	public AllianceCelebrateLevelCfg() {
		lv = 0;
		allianceExp = 0;
		rewardPt1 = "";
		personExpPt1 = 0;
		rewardPt2 = "";
		personExpPt2 = 0;
	}
	
	@Override
	protected boolean assemble() {
		awardItem1 = RewardHelper.toRewardItemImmutableList(rewardPt1);
		awardItem2 = RewardHelper.toRewardItemImmutableList(rewardPt2);
		return true;
	}

	public int getLv() {
		return lv;
	}

	public int getAllianceExp() {
		return allianceExp;
	}

	public String getRewardPt1() {
		return rewardPt1;
	}

	public int getPersonExpPt1() {
		return personExpPt1;
	}

	public String getRewardPt2() {
		return rewardPt2;
	}

	public int getPersonExpPt2() {
		return personExpPt2;
	}

	public List<Reward.RewardItem.Builder> getAwardItem1() {
		return awardItem1;
	}

	public List<Reward.RewardItem.Builder> getAwardItem2() {
		return awardItem2;
	}
}

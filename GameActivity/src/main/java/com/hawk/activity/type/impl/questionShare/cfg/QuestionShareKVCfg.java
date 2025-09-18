package com.hawk.activity.type.impl.questionShare.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/question_share/question_share_cfg.xml")
public class QuestionShareKVCfg extends HawkConfigBase {

	/** 是否跨天重置，1重置0不重置 **/
	private final int isReset;

	/** 回答正确奖励 **/
	private final String rightReward;

	/** 回答错误奖励 **/
	private final String wrongReward;

	/** 每日可答题次数 **/
	private final int questionTime;

	/** 答对奖励邮件id */
	private final int rightMail;

	/** 答错奖励邮件id */
	private final int wrongMail;

	/** 每日奖励 */
	private final String dayReward;
	private List<RewardItem.Builder> rightAwardItems;

	private List<RewardItem.Builder> wrongAwardItems;

	private List<RewardItem.Builder> dayAwardItems;
	private static QuestionShareKVCfg instance;

	public static QuestionShareKVCfg getInstance() {
		return instance;
	}

	public QuestionShareKVCfg() {
		isReset = 0;
		questionTime = 1;
		instance = this;
		rightReward = "";
		wrongReward = "";
		rightMail = 20190231;
		wrongMail = 20190232;
		dayReward = "";
	}

	public int getIsReset() {
		return isReset;
	}

	public boolean isReset() {
		return isReset == 1;
	}

	public int getQuestionTime() {
		return questionTime;
	}

	public String getRightReward() {
		return rightReward;
	}

	public String getWrongReward() {
		return wrongReward;
	}

	public List<RewardItem.Builder> getRightAwardItems() {
		return rightAwardItems;
	}

	public List<RewardItem.Builder> getWrongAwardItems() {
		return wrongAwardItems;
	}
	public List<RewardItem.Builder> getDailyAwardItems() {
		return dayAwardItems;
	}
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected boolean assemble() {
		try {
			rightAwardItems = RewardHelper.toRewardItemImmutableList(rightReward);
			wrongAwardItems = RewardHelper.toRewardItemImmutableList(wrongReward);
			dayAwardItems = RewardHelper.toRewardItemImmutableList(dayReward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		if (isReset != 0 && isReset != 1) {
			throw new RuntimeException(String.format("question_share_cfg.xml 配置isReset出错:%d", isReset));
		}
		return super.checkValid();
	}

	public int getRightMail() {
		return rightMail;
	}

	public int getWrongMail() {
		return wrongMail;
	}

	public String getDailyReward() {
		return dayReward;
	}
}

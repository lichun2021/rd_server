package com.hawk.activity.type.impl.globalSign.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/ssy_full_signin/ssy_full_signin_cfg.xml")
public class GlobalSignActivityKVCfg extends HawkConfigBase {
	private final int serverDelay;
	
	//刷新时间
	private final int refreshTime;
	
	//签到系数数
	private final int signinfactorQQ;
	private final int signinfactorWX;

	//签到基数
	private final int signinAssistBaseQQ;
	private final int signinAssistBaseWX;

	//定时注水
	private final String signinAssistIntervalQQ;
	private final String signinAssistIntervalWX;
	//签到固定奖励
	private final String signRewards;
	private final int randomRewards;
	//弹幕个数
	private final int bulletChatCount;
	private final String blessingWords;
	
	private List<int[]> signinAssistListQQ = new ArrayList<>();
	private List<int[]> signinAssistListWX = new ArrayList<>();
	private List<RewardItem.Builder> signRewardList;
	private List<String> blessingWordList;
	public GlobalSignActivityKVCfg() {
		this.serverDelay = 0;
		this.refreshTime = 0;
		this.signinfactorQQ = 0;
		this.signinfactorWX = 0;
		this.signinAssistBaseQQ = 0;
		this.signinAssistBaseWX = 0;
		this.signinAssistIntervalQQ = "";
		this.signinAssistIntervalWX = "";
		signRewards = "";
		randomRewards = 0;
		bulletChatCount = 30;
		blessingWords = "";
	}

	@Override
	protected boolean assemble() {
		signinAssistListQQ = SerializeHelper.str2intList(this.signinAssistIntervalQQ);
		signinAssistListWX = SerializeHelper.str2intList(this.signinAssistIntervalWX);
		signRewardList = RewardHelper.toRewardItemImmutableList(signRewards);
		blessingWordList = SerializeHelper.stringToList(String.class, blessingWords, SerializeHelper.BETWEEN_ITEMS);
		return true;
	}
	
	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public int getSigninfactorQQ() {
		return signinfactorQQ;
	}

	public int getSigninfactorWX() {
		return signinfactorWX;
	}

	public int getSigninAssistBaseQQ() {
		return signinAssistBaseQQ;
	}

	public int getSigninAssistBaseWX() {
		return signinAssistBaseWX;
	}

	public List<int[]> getSigninAssistListQQ() {
		return signinAssistListQQ;
	}

	public List<int[]> getSigninAssistListWX() {
		return signinAssistListWX;
	}

	public int getRefreshTime() {
		return refreshTime;
	}

	public int getBulletChatCount() {
		return bulletChatCount;
	}

	public List<RewardItem.Builder> getSignRewardList() {
		return signRewardList;
	}

	public int getRandomRewards() {
		return randomRewards;
	}

	public List<String> getBlessingWordList() {
		return blessingWordList;
	}

	
	
	

}

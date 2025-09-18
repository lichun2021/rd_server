package com.hawk.activity.type.impl.heroTrial;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.HeroGoTrialReq;
import com.hawk.game.protocol.Activity.HeroTrialGetReward;
import com.hawk.game.protocol.Activity.HeroTrialSpeedUp;

/**
 * 英雄试炼
 * @author golden
 *
 */
public class HeroTrialHandler extends ActivityProtocolHandler {

	/**
	 * 获取界面信息
	 */
	@ProtocolHandler(code = HP.code.HERO_TRIAL_PAGE_INFO_REQ_VALUE)
	public boolean getPageInfo(HawkProtocol protocol, String playerId) {
		HeroTrialActivity activity = getActivity(ActivityType.HERO_TRIAL);
		activity.pushPageInfo(playerId);
		return true;
	}
	
	/**
	 * 刷新界面
	 */
	@ProtocolHandler(code = HP.code.HERO_TRIAL_REFRESH_REQ_VALUE)
	public boolean refreshMission(HawkProtocol protocol, String playerId) {
		HeroTrialActivity activity = getActivity(ActivityType.HERO_TRIAL);
		activity.refreshPage(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 英雄试炼
	 */
	@ProtocolHandler(code = HP.code.HERO_GO_TRIAL_REQ_VALUE)
	public boolean heroTrial(HawkProtocol protocol, String playerId) {
		HeroTrialActivity activity = getActivity(ActivityType.HERO_TRIAL);
		HeroGoTrialReq req = protocol.parseProtocol(HeroGoTrialReq.getDefaultInstance());
		activity.goTrial(playerId, protocol.getType(), req.getMissionUUid(), req.getHeroIdsList());
		return true;
	}
	
	/**
	 * 英雄试炼加速
	 */
	@ProtocolHandler(code = HP.code.HERO_TRIAL_SPEED_UP_VALUE)
	public boolean speedUp(HawkProtocol protocol, String playerId) {
		HeroTrialActivity activity = getActivity(ActivityType.HERO_TRIAL);
		HeroTrialSpeedUp req = protocol.parseProtocol(HeroTrialSpeedUp.getDefaultInstance());
		activity.speedUp(playerId, protocol.getType(), req.getMissionUUid());
		return true;
	}
	
	/**
	 * 领取任务奖励
	 */
	@ProtocolHandler(code = HP.code.HERO_TRIAL_GET_REWARD_VALUE)
	public boolean getReward(HawkProtocol protocol, String playerId) {
		HeroTrialActivity activity = getActivity(ActivityType.HERO_TRIAL);
		HeroTrialGetReward req = protocol.parseProtocol(HeroTrialGetReward.getDefaultInstance());
		activity.getMissionReward(playerId, protocol.getType(), req.getMissionUUid());
		return true;
	}
}

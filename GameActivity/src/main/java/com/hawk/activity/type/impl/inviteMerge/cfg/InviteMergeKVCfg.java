package com.hawk.activity.type.impl.inviteMerge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/merge_invite/merge_invite_cfg.xml")
public class InviteMergeKVCfg extends HawkConfigBase {

	/**
	 * 去兵战力前X名的玩家可投票
	 */
	private final int votingPlayerNum;
	/**
	 * 在time表startTime之前的limit时间（单位秒）范围内，可以选leader。（邀请互通活动开启时间为周四，当天有盟总，邀请互通活动改为周五开启。让周四的盟总结果对邀请互通有影响）
	 */
	private final int chooseLeaderTimeLimit;

	public InviteMergeKVCfg() {
		votingPlayerNum = 100;
		chooseLeaderTimeLimit = 300;
	}

	public int getVotingPlayerNum() {
		return votingPlayerNum;
	}
	
	public long getChooseLeaderTimeLimit() {
		return chooseLeaderTimeLimit * 1000L;
	}
}

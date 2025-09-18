package com.hawk.activity.type.impl.militaryprepare;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.MilitaryPrepareAchieveRewardReq;
import com.hawk.game.protocol.HP;

public class MilitaryPrepareHandler extends ActivityProtocolHandler {
	
	/***
	 * 查看界面
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.MILITARY_PREPARE_INFO_REQ_VALUE)
	public void globalSignInfo(HawkProtocol protocol, String playerId){
		MilitaryPrepareActivity activity = getActivity(ActivityType.MILITARY_PREPARE_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
	}
	
	
	/**
	 * 补领进阶奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.MILITARY_PREPARE_BOX_REWARD_REQ_VALUE)
	public void settingBulletChat(HawkProtocol protocol, String playerId){
		MilitaryPrepareActivity activity = getActivity(ActivityType.MILITARY_PREPARE_ACTIVITY);
		MilitaryPrepareAchieveRewardReq req = protocol.parseProtocol(
				 MilitaryPrepareAchieveRewardReq.getDefaultInstance());
		activity.achieveBoxReward(playerId, req.getBox());
	}
	
	
}

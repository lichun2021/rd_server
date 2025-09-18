package com.hawk.activity.type.impl.backFlow.privilege;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 体力赠送活动
 * 
 * @author che
 *
 */
public class PrivilegeActivityHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code.BACK_PRIVILEGE_INFO_REQ_VALUE)
	public void getShowInfo(HawkProtocol hawkProtocol, String playerId){
		PrivilegeActivity activity = this.getActivity(ActivityType.BACK_PRIVILEGE);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.BACK_PRIVILEGE_REWARD_REQ_VALUE)
	public void getReward(HawkProtocol hawkProtocol, String playerId){
		PrivilegeActivity activity = this.getActivity(ActivityType.BACK_PRIVILEGE);
		if(activity == null){
			return;
		}
		activity.reward(playerId);
	}
}
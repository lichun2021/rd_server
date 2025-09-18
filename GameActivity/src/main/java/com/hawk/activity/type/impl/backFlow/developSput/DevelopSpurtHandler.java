package com.hawk.activity.type.impl.backFlow.developSput;

import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/***
 * 老玩家回归 C->S
 * @author yang.rao
 *
 */
public class DevelopSpurtHandler extends ActivityProtocolHandler {
	
	/**
	 * 登录签到
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.DEVELOP_SPURT_SIGN_REQ_VALUE)
	public void DevelopSpurtSignIn(HawkProtocol protocol, String playerId){
		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.DEVELOP_SPURT_VALUE);
		if (!optional.isPresent()){
			return;
		}
		
		DevelopSpurtActivity activity = (DevelopSpurtActivity) optional.get();
		int result = activity.signIn(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
}

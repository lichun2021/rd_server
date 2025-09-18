package com.hawk.game.invoker.warcollege;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;
import org.hawk.os.HawkException;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.warcollege.WarCollegeInstanceService;

public class WarCollegeInstanceEnterRpcInvoker extends HawkRpcInvoker {
	
	Player player;
	public WarCollegeInstanceEnterRpcInvoker(Player player) {
		this.player = player;
	}
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		try {
			WarCollegeInstanceService instance = (WarCollegeInstanceService)targetObj;
			int errorCode = instance.onInstanceEnter(player);
			result.put("errorCode", errorCode);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e, targetObj,msg,result);
			return false;
		}
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		try {
			int errorCode = (int)result.get("errorCode");
			if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
				player.responseSuccess(HP.code.WAR_COLLEGE_ENTER_INSTANCE_REQ_VALUE);
			} else {
				player.sendError(HP.code.WAR_COLLEGE_ENTER_INSTANCE_REQ_VALUE, errorCode, 0);
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e, callerObj,result);
			return false;
		}
	}

}

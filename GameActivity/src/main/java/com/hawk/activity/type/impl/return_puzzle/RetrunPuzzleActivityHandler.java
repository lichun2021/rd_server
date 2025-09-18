package com.hawk.activity.type.impl.return_puzzle;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.event.impl.GroupPurchaseEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.ReturnPuzzlePageInfo;
import com.hawk.game.protocol.HP;

/**
 * 武者拼图
 * @author Jesse
 */
public class RetrunPuzzleActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 进入活动界面
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RETURN_PUZZLE_GET_PAGE_INFO_C_VALUE)
	public boolean onEnterPage(HawkProtocol protocol, String playerId) {
		ActivityManager.getInstance().postEvent(new GroupPurchaseEvent(playerId));
		ReturnPuzzleActivity activity = getActivity(ActivityType.RETURN_PUZZLE_ACTIVITY);
		ReturnPuzzlePageInfo.Builder builder = activity.genPageInfo(playerId);
		if(builder != null){
			sendProtocol(playerId, HawkProtocol.valueOf(HP.code.RETURN_PUZZLE_GET_PAGE_INFO_S_VALUE, builder));
		}
		return true;
	}

}

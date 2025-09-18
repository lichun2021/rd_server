package com.hawk.activity.type.impl.machineAwake;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DamageRankType;
import com.hawk.game.protocol.Activity.GetDamageRankInfo;
import com.hawk.game.protocol.HP;

/**
 * 机甲觉醒
 * @author Jesse
 *
 */
public class MachineAwakeHandler extends ActivityProtocolHandler {

	/**
	 * 获取活动界面信息
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MACHINE_AWAKE_GET_PAGE_INFO_C_VALUE)
	public boolean onGetStageInfo(HawkProtocol protocol, String playerId) {
		MachineAwakeActivity activity = getActivity(ActivityType.MACHINE_AWAKE_ACTIVITY);
		activity.pullPageInfo(playerId);
		return true;
	}
	
	/**
	 * 获取排行榜单信息
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MACHINE_AWAKE_GET_RANK_INFO_C_VALUE)
	public boolean onGetHistoryRankList(HawkProtocol protocol, String playerId) {
		GetDamageRankInfo req = protocol.parseProtocol(GetDamageRankInfo.getDefaultInstance());
		DamageRankType rankType = req.getRankType();
		MachineAwakeActivity activity = getActivity(ActivityType.MACHINE_AWAKE_ACTIVITY);
		activity.pullRankInfo(playerId, rankType);
		return true;
	}
}

package com.hawk.activity.type.impl.energies;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.EnergiesRankType;
import com.hawk.game.protocol.Activity.GetEnergiesRankInfo;
import com.hawk.game.protocol.HP;

/**
 * 机甲觉醒
 * @author Jesse
 *
 */
public class EnergiesHandler extends ActivityProtocolHandler {

	/**
	 * 获取活动界面信息
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ENERGIES_GET_PAGE_INFO_C_VALUE)
	public boolean onGetStageInfo(HawkProtocol protocol, String playerId) {
		EnergiesActivity activity = getActivity(ActivityType.ENERGIES_ACTIVITY);
		activity.pullPageInfo(playerId);
		return true;
	}
	
	/**
	 * 获取排行榜单信息
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ENERGIES_GET_RANK_INFO_C_VALUE)
	public boolean onGetHistoryRankList(HawkProtocol protocol, String playerId) {
		GetEnergiesRankInfo req = protocol.parseProtocol(GetEnergiesRankInfo.getDefaultInstance());
		EnergiesRankType rankType = req.getRankType();
		EnergiesActivity activity = getActivity(ActivityType.ENERGIES_ACTIVITY);
		activity.pullRankInfo(playerId, rankType);
		return true;
	}
}

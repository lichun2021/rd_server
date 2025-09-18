package com.hawk.activity.type.impl.stronestleader;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 最强指挥官
 * @author PhilChen
 *
 */
public class StrongestLeaderActivityHandler extends ActivityProtocolHandler {

	/**
	 * 获取当前阶段所有信息
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PULL_LEADER_STAGE_INFO_C_VALUE)
	public boolean onGetStageInfo(HawkProtocol protocol, String playerId) {
		StrongestLeaderActivity activity = getActivity(ActivityType.STRONGEST_LEADER);
		activity.pushStageInfo(playerId);
		return true;
	}
	
	/**
	 * 获取历史最强指挥官每期信息
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PULL_LEADER_HISTORY_RANK_LIST_C_VALUE)
	public boolean onGetHistoryRankList(HawkProtocol protocol, String playerId) {
		StrongestLeaderActivity activity = getActivity(ActivityType.STRONGEST_LEADER);
		activity.pushHistoryRankList(playerId);
		return true;
	}
}

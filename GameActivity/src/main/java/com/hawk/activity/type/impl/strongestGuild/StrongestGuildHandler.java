package com.hawk.activity.type.impl.strongestGuild;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.StrongestGuildRewardReq;
import com.hawk.game.protocol.HP;

public class StrongestGuildHandler extends ActivityProtocolHandler {

	/***
	 * 请求界面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.STRONGEST_GUILD_INFO_C_VALUE)
	public void onPlayerReqInfo(HawkProtocol protocol, String playerId){
		StrongestGuildActivity activity = getActivity(ActivityType.STRONGEST_GUILD_ACTIVITY);
		if(activity != null){
			activity.syncActivityInfo(playerId);
		}
	}
	
	/***
	 * 请求个人榜
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.STRONGEST_GUILD_PERSONAL_RANK_C_VALUE)
	public void onPlayerReqPersonRank(HawkProtocol protocol, String playerId){
		StrongestGuildActivity activity = getActivity(ActivityType.STRONGEST_GUILD_ACTIVITY);
		if(activity != null){
			activity.syncPersonRankInfo(playerId);
		}
	}
	
	/***
	 * 请求联盟排行榜
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.STRONGEST_GUILD_GUILD_RANK_C_VALUE)
	public void onPlayerReqGuildRank(HawkProtocol protocol, String playerId){
		StrongestGuildActivity activity = getActivity(ActivityType.STRONGEST_GUILD_ACTIVITY);
		if(activity != null){
			activity.syncGuildRankInfo(playerId);
		}
	}
	
	/***
	 * 领取个人奖励
	 * @param protocol
	 * @param playerId
	 */
	
	@ProtocolHandler(code = HP.code.STRONGEST_GUILD_REWARD_REQ_C_VALUE)
	public void onPlayerGetReward(HawkProtocol protocol, String playerId){
		StrongestGuildActivity activity = getActivity(ActivityType.STRONGEST_GUILD_ACTIVITY);
		StrongestGuildRewardReq req = protocol.parseProtocol(StrongestGuildRewardReq.getDefaultInstance());
		int targetId = req.getTargetId();
		Result<?> result = activity.reqPersonStageReward(playerId, targetId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, HP.code.STRONGEST_GUILD_HISTORY_REQ_C_VALUE, result.getStatus());
		}
	}
	
	/***
	 * 玩家请求历史排行榜信息
	 * @param protocol
	 * @param playerId
	 */
	
	@ProtocolHandler(code = HP.code.STRONGEST_GUILD_HISTORY_REQ_C_VALUE)
	public void onPlayerGetHistoryRank(HawkProtocol protocol, String playerId){
		StrongestGuildActivity activity = getActivity(ActivityType.STRONGEST_GUILD_ACTIVITY);
		activity.sendHistoryRank(playerId);
	}
}

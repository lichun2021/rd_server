package com.hawk.activity.type.impl.seasonpuzzle;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.CallHelpInfoReq;
import com.hawk.game.protocol.Activity.CallHelpReq;
import com.hawk.game.protocol.Activity.PuzzleSetReq;
import com.hawk.game.protocol.Activity.SendItemReq;
import com.hawk.game.protocol.HP;

/**
 * 赛季拼图373
 * 
 * @author lating
 */
public class SeasonPuzzleHandler extends ActivityProtocolHandler{

	/**
	 * 请求活动信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.SEASON_PUZZLE_ACT_INFO_C_VALUE)
	public void onActivityInfoReq(HawkProtocol protocol, String playerId){
		SeasonPuzzleActivity activity = getActivity(ActivityType.SEASON_PUZZLE_373);
		if(activity != null && activity.isOpening(playerId)){
			activity.syncActivityDataInfo(playerId);
		}
	}
	
	/**
	 * 查看求助信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.SEASON_PUZZLE_CALL_INFO_C_VALUE)
	public void queryCallHelpInfo(HawkProtocol protocol, String playerId){
		SeasonPuzzleActivity activity = getActivity(ActivityType.SEASON_PUZZLE_373);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		CallHelpInfoReq req = protocol.parseProtocol(CallHelpInfoReq.getDefaultInstance());
		int result = activity.queryCallHelpInfo(playerId, req.getType(), req.getPage());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 发起求助
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.SEASON_PUZZLE_CALL_HELP_C_VALUE)
	public void callHelpReq(HawkProtocol protocol, String playerId){
		SeasonPuzzleActivity activity = getActivity(ActivityType.SEASON_PUZZLE_373);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		CallHelpReq req = protocol.parseProtocol(CallHelpReq.getDefaultInstance());
		int result = activity.callHelp(playerId, req.getType(), req.getItemId(), req.getTarPlayerId());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 放入碎片进行拼图
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.SEASON_PUZZLE_SET_ITEM_C_VALUE)
	public void setPuzzleItemReq(HawkProtocol protocol, String playerId){
		SeasonPuzzleActivity activity = getActivity(ActivityType.SEASON_PUZZLE_373);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		PuzzleSetReq req = protocol.parseProtocol(PuzzleSetReq.getDefaultInstance());
		int result = activity.setPuzzleItem(playerId, req.getSetIndexList());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 赠送拼图碎片
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.SEASON_PUZZLE_SEND_ITEM_C_VALUE)
	public void sendPuzzleItemReq(HawkProtocol protocol, String playerId){
		SeasonPuzzleActivity activity = getActivity(ActivityType.SEASON_PUZZLE_373);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		SendItemReq req = protocol.parseProtocol(SendItemReq.getDefaultInstance());
		int result = activity.sendPuzzleItem(playerId, req.getUuid());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
}

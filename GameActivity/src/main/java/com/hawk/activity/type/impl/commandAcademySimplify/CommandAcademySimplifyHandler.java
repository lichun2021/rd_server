package com.hawk.activity.type.impl.commandAcademySimplify;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.CommandCollegeBuyPackageReq;
import com.hawk.game.protocol.HP;

public class CommandAcademySimplifyHandler extends ActivityProtocolHandler {
	
	/**
	 * 指挥官学院页面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMAND_COLLEGE_SIMPLIFY_INFO_REQ_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId){
		CommandAcademySimplifyActivity activity = getActivity(ActivityType.COMMAND_ACADEMY_SIMPLIFY_ACTIVITY);
		if(activity == null){
			return;
		}
		
		activity.syncActivityDataInfo(playerId);
	}
	
	
	/**
	 * 指挥官学院阶段榜信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMAND_COLLEGE_SIMPLIFY_STAGE_RANK_REQ_VALUE)
	public void onPlayerStageRank(HawkProtocol protocol, String playerId){
		CommandAcademySimplifyActivity activity = getActivity(ActivityType.COMMAND_ACADEMY_SIMPLIFY_ACTIVITY);
		activity.getStageRankInfos(playerId);
	}
	
	
	/**
	 * 指挥官学院总评榜信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMAND_COLLEGE_SIMPLIFY_FINAL_RANK_REQ_VALUE)
	public void onPlayerFianlRank(HawkProtocol protocol, String playerId){
		CommandAcademySimplifyActivity activity = getActivity(ActivityType.COMMAND_ACADEMY_SIMPLIFY_ACTIVITY);
		activity.getFinalRankInfo(playerId);
	}
	

	/**
	 * 指挥官学院购买助力礼包
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMAND_COLLEGE_SIMPLIFY_BUY_PACKAGE_REQ_VALUE)
	public void onPlayerBuyPackage(HawkProtocol protocol, String playerId){
		CommandAcademySimplifyActivity activity = getActivity(ActivityType.COMMAND_ACADEMY_SIMPLIFY_ACTIVITY);
		CommandCollegeBuyPackageReq req = protocol.parseProtocol(CommandCollegeBuyPackageReq
				.getDefaultInstance());
		int packageId = req.getPackageId();
		activity.buyPackage(playerId, packageId,protocol.getType());
	}
}

package com.hawk.activity.type.impl.commandAcademy;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.event.impl.EnergyCountEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.CommandCollegeBuyPackageReq;
import com.hawk.game.protocol.HP;

public class CommandAcademyHandler extends ActivityProtocolHandler {
	
	/**
	 * 指挥官学院页面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMAND_COLLEGE_INFO_REQ_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId){
		CommandAcademyActivity activity = getActivity(ActivityType.COMMAND_ACADEMY_ACTIVITY);
		if(activity == null){
			return;
		}
		//推送能量数量
		{
			int eCount = activity.getDataGeter().getEnergyCount(playerId);
			EnergyCountEvent event = new EnergyCountEvent(playerId, eCount);
			//刷榜
			activity.onEnergyCountEvent(event);
			//推成就
			ActivityManager.getInstance().postEvent(event);
			
		}
		
		activity.syncActivityDataInfo(playerId);
		
		
	}
	
	
	/**
	 * 指挥官学院阶段榜信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMAND_COLLEGE_STAGE_RANK_REQ_VALUE)
	public void onPlayerStageRank(HawkProtocol protocol, String playerId){
		CommandAcademyActivity activity = getActivity(ActivityType.COMMAND_ACADEMY_ACTIVITY);
		activity.getStageRankInfos(playerId);
	}
	
	
	/**
	 * 指挥官学院总评榜信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMAND_COLLEGE_FINAL_RANK_REQ_VALUE)
	public void onPlayerFianlRank(HawkProtocol protocol, String playerId){
		CommandAcademyActivity activity = getActivity(ActivityType.COMMAND_ACADEMY_ACTIVITY);
		activity.getFinalRankInfo(playerId);
	}
	

	/**
	 * 指挥官学院购买助力礼包
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMAND_COLLEGE_BUY_PACKAGE_REQ_VALUE)
	public void onPlayerBuyPackage(HawkProtocol protocol, String playerId){
		CommandAcademyActivity activity = getActivity(ActivityType.COMMAND_ACADEMY_ACTIVITY);
		CommandCollegeBuyPackageReq req = protocol.parseProtocol(CommandCollegeBuyPackageReq
				.getDefaultInstance());
		int packageId = req.getPackageId();
		activity.buyPackage(playerId, packageId,protocol.getType());
	}
}
